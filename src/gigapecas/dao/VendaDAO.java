package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import gigapecas.model.FormaPagamento;
import gigapecas.model.ItemVenda;
import gigapecas.model.Venda;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    
    public int registrar(List<ItemVenda> itens, String usuarioVendedor, FormaPagamento forma) {
        String sqlVenda = "INSERT INTO venda (data_hora, funcionario_id, total, forma_pagamento) "
                + "VALUES (?, (SELECT id FROM funcionario WHERE usuario = ?), ?, ?)";
        String sqlItem = "INSERT INTO item_venda (venda_id, peca_id, nome_peca, quantidade, preco_unitario, custo_unitario) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlBaixa = "UPDATE peca SET quantidade_estoque = quantidade_estoque - ? "
                + "WHERE id = ? AND quantidade_estoque >= ?";

        double total = 0;
        for (ItemVenda i : itens) total += i.getSubtotal();

        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stItem = conn.prepareStatement(sqlItem);
                 PreparedStatement stBaixa = conn.prepareStatement(sqlBaixa)) {

                stVenda.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                stVenda.setString(2, usuarioVendedor);
                stVenda.setDouble(3, total);
                stVenda.setString(4, forma.name());
                stVenda.executeUpdate();

                int vendaId;
                try (ResultSet rs = stVenda.getGeneratedKeys()) {
                    rs.next();
                    vendaId = rs.getInt(1);
                }

                for (ItemVenda i : itens) {
                    stBaixa.setInt(1, i.getQuantidade());
                    stBaixa.setInt(2, i.getPecaId());
                    stBaixa.setInt(3, i.getQuantidade());
                    if (stBaixa.executeUpdate() == 0) {
                        throw new SQLException("Estoque insuficiente para \"" + i.getNomePeca() + "\".");
                    }

                    stItem.setInt(1, vendaId);
                    stItem.setInt(2, i.getPecaId());
                    stItem.setString(3, i.getNomePeca());
                    stItem.setInt(4, i.getQuantidade());
                    stItem.setDouble(5, i.getPrecoUnitario());
                    stItem.setDouble(6, i.getCustoUnitario());
                    stItem.executeUpdate();

                    PecaDAO.registrarMovimentacao(conn, i.getPecaId(), "SAIDA", i.getQuantidade(),
                            usuarioVendedor, null, vendaId);
                }

                conn.commit();
                return vendaId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar venda: " + e.getMessage());
        }
    }

    public List<Venda> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        String sql = "SELECT v.id, v.data_hora, v.total, v.forma_pagamento, "
                + "COALESCE(f.nome, '(removido)') AS vendedor, "
                + "COALESCE(SUM(i.quantidade), 0) AS itens, "
                + "COALESCE(SUM(i.quantidade * i.custo_unitario), 0) AS custo "
                + "FROM venda v "
                + "LEFT JOIN funcionario f ON f.id = v.funcionario_id "
                + "LEFT JOIN item_venda i ON i.venda_id = v.id "
                + "WHERE v.data_hora >= ? AND v.data_hora < ? "
                + "GROUP BY v.id, v.data_hora, v.total, v.forma_pagamento, f.nome "
                + "ORDER BY v.data_hora DESC";
        List<Venda> vendas = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            definirPeriodo(st, inicio, fim);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Venda v = new Venda(
                            rs.getInt("id"),
                            rs.getTimestamp("data_hora").toLocalDateTime(),
                            rs.getString("vendedor"),
                            rs.getInt("itens"),
                            rs.getDouble("total"),
                            rs.getDouble("custo"));
                    v.setFormaPagamento(FormaPagamento.valueOf(rs.getString("forma_pagamento")));
                    vendas.add(v);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar vendas: " + e.getMessage());
        }
        return vendas;
    }

    public List<ItemVenda> listarItens(int vendaId) {
        String sql = "SELECT peca_id, nome_peca, quantidade, preco_unitario, custo_unitario "
                + "FROM item_venda WHERE venda_id = ?";
        List<ItemVenda> itens = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, vendaId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    itens.add(new ItemVenda(rs.getInt("peca_id"), rs.getString("nome_peca"),
                            rs.getInt("quantidade"), rs.getDouble("preco_unitario"), rs.getDouble("custo_unitario")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar itens da venda: " + e.getMessage());
        }
        return itens;
    }

    public List<ItemVenda> maisVendidas(LocalDate inicio, LocalDate fim, int limite) {
        String sql = "SELECT i.nome_peca, SUM(i.quantidade) AS qtd, SUM(i.quantidade * i.preco_unitario) AS valor "
                + "FROM item_venda i JOIN venda v ON v.id = i.venda_id "
                + "WHERE v.data_hora >= ? AND v.data_hora < ? "
                + "GROUP BY i.nome_peca ORDER BY qtd DESC";
        List<ItemVenda> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            definirPeriodo(st, inicio, fim);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next() && lista.size() < limite) {
                    lista.add(new ItemVenda(0, rs.getString("nome_peca"), rs.getInt("qtd"), rs.getDouble("valor"), 0));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar mais vendidas: " + e.getMessage());
        }
        return lista;
    }

    private void definirPeriodo(PreparedStatement st, LocalDate inicio, LocalDate fim) throws SQLException {
        st.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
        st.setTimestamp(2, Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
    }
}
