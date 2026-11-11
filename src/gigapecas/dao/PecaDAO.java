package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import gigapecas.model.Peca;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PecaDAO {

  
    public void inserir(Peca peca) {
        String sql = "INSERT INTO peca (codigo_barras, nome, aplicacao, preco_custo, preco_venda, quantidade_estoque, estoque_minimo) VALUES (?, ?, ?, ?, ?, ?, 5)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, peca.getCodigoBarras());
            stmt.setString(2, peca.getNome());
            stmt.setString(3, peca.getAplicacao());
            stmt.setDouble(4, peca.getPrecoCusto());
            stmt.setDouble(5, peca.getPrecoVenda());
            stmt.setInt(6, peca.getQuantidadeEstoque());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir peça: " + e.getMessage());
        }
    }

   
    public List<Peca> listar() {
        String sql = "SELECT * FROM peca";
        List<Peca> pecas = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Peca p = new Peca(
                    rs.getInt("id"),
                    rs.getString("codigo_barras"),
                    rs.getString("nome"),
                    rs.getString("aplicacao"),
                    rs.getDouble("preco_custo"),
                    rs.getDouble("preco_venda"),
                    rs.getInt("quantidade_estoque")
                );
                pecas.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar peças: " + e.getMessage());
        }
        return pecas;
    }

    public void atualizarEstoque(int id, int novaQuantidade) {
        String sql = "UPDATE peca SET quantidade_estoque = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novaQuantidade);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o estoque: " + e.getMessage());
        }
    }

    
    public void excluir(int id) {
        String sql = "DELETE FROM peca WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir peça: " + e.getMessage());
        }
    }
}