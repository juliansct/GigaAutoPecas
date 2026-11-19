package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import gigapecas.model.MovimentacaoEstoque;
import gigapecas.model.Peca;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PecaDAO {

   
    static void registrarMovimentacao(Connection conn, int pecaId, String tipo, int quantidade,
                                      String usuario, Integer fornecedorId, Integer vendaId) throws SQLException {
        String sql = "INSERT INTO movimentacao_estoque (peca_id, tipo, quantidade, funcionario_id, fornecedor_id, venda_id) "
                + "VALUES (?, ?, ?, (SELECT id FROM funcionario WHERE usuario = ?), ?, ?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, pecaId);
            st.setString(2, tipo);
            st.setInt(3, quantidade);
            st.setString(4, usuario);
            st.setObject(5, fornecedorId, Types.INTEGER);
            st.setObject(6, vendaId, Types.INTEGER);
            st.executeUpdate();
        }
    }

    
    public void inserir(Peca peca, String usuario) {
        String sql = "INSERT INTO peca (codigo_barras, nome, aplicacao, preco_custo, preco_venda, "
                + "quantidade_estoque, estoque_minimo, categoria_id, fornecedor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preencherCampos(st, peca);
                st.setInt(6, peca.getQuantidadeEstoque());
                st.setInt(7, peca.getEstoqueMinimo());
                st.setObject(8, peca.getCategoriaId(), Types.INTEGER);
                st.setObject(9, peca.getFornecedorId(), Types.INTEGER);
                st.executeUpdate();

                try (ResultSet rs = st.getGeneratedKeys()) {
                    rs.next();
                    peca.setId(rs.getInt(1));
                }
                if (peca.getQuantidadeEstoque() > 0) {
                    registrarMovimentacao(conn, peca.getId(), "ENTRADA", peca.getQuantidadeEstoque(),
                            usuario, peca.getFornecedorId(), null);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar peça: " + traduzirErro(e));
        }
    }

    public List<Peca> listar() {
        String sql = "SELECT p.*, c.nome AS categoria_nome, f.nome_fantasia AS fornecedor_nome "
                + "FROM peca p "
                + "LEFT JOIN categoria c ON c.id = p.categoria_id "
                + "LEFT JOIN fornecedor f ON f.id = p.fornecedor_id "
                + "ORDER BY p.nome";
        List<Peca> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Peca p = new Peca(
                        rs.getInt("id"),
                        rs.getString("codigo_barras"),
                        rs.getString("nome"),
                        rs.getString("aplicacao"),
                        rs.getDouble("preco_custo"),
                        rs.getDouble("preco_venda"),
                        rs.getInt("quantidade_estoque"));
                p.setEstoqueMinimo(rs.getInt("estoque_minimo"));
                p.setCategoriaId((Integer) rs.getObject("categoria_id", Integer.class));
                p.setFornecedorId((Integer) rs.getObject("fornecedor_id", Integer.class));
                p.setCategoriaNome(rs.getString("categoria_nome"));
                p.setFornecedorNome(rs.getString("fornecedor_nome"));
                lista.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar peças: " + e.getMessage());
        }
        return lista;
    }

   
    public void atualizar(Peca peca) {
        String sql = "UPDATE peca SET codigo_barras = ?, nome = ?, aplicacao = ?, preco_custo = ?, preco_venda = ?, "
                + "estoque_minimo = ?, categoria_id = ?, fornecedor_id = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            preencherCampos(st, peca);
            st.setInt(6, peca.getEstoqueMinimo());
            st.setObject(7, peca.getCategoriaId(), Types.INTEGER);
            st.setObject(8, peca.getFornecedorId(), Types.INTEGER);
            st.setInt(9, peca.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar peça: " + traduzirErro(e));
        }
    }

   
    public void reporEstoque(int pecaId, int quantidade, Integer fornecedorId, String usuario) {
        String sql = "UPDATE peca SET quantidade_estoque = quantidade_estoque + ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, quantidade);
                st.setInt(2, pecaId);
                st.executeUpdate();
                registrarMovimentacao(conn, pecaId, "ENTRADA", quantidade, usuario, fornecedorId, null);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao repor estoque: " + e.getMessage());
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM peca WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir peça: " + e.getMessage());
        }
    }

    
    public List<MovimentacaoEstoque> listarMovimentacoes(int pecaId) {
        String sql = "SELECT m.data_hora, m.tipo, m.quantidade, m.venda_id, "
                + "f.nome_fantasia AS fornecedor, fu.nome AS funcionario "
                + "FROM movimentacao_estoque m "
                + "LEFT JOIN fornecedor f ON f.id = m.fornecedor_id "
                + "LEFT JOIN funcionario fu ON fu.id = m.funcionario_id "
                + "WHERE m.peca_id = ? ORDER BY m.data_hora DESC, m.id DESC";
        List<MovimentacaoEstoque> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, pecaId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(new MovimentacaoEstoque(
                            rs.getTimestamp("data_hora").toLocalDateTime(),
                            rs.getString("tipo"),
                            rs.getInt("quantidade"),
                            rs.getString("fornecedor"),
                            (Integer) rs.getObject("venda_id", Integer.class),
                            rs.getString("funcionario")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar histórico: " + e.getMessage());
        }
        return lista;
    }

    
    private void preencherCampos(PreparedStatement st, Peca p) throws SQLException {
        String codigo = p.getCodigoBarras() == null || p.getCodigoBarras().isBlank() ? null : p.getCodigoBarras().trim();
        st.setString(1, codigo);
        st.setString(2, p.getNome());
        st.setString(3, p.getAplicacao());
        st.setDouble(4, p.getPrecoCusto());
        st.setDouble(5, p.getPrecoVenda());
    }

    private String traduzirErro(SQLException e) {
        if (e.getErrorCode() == 1062) return "já existe uma peça com esse código de barras.";
        return e.getMessage();
    }
}
