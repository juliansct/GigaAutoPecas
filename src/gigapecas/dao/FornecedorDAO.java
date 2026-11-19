package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import gigapecas.model.Fornecedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDAO {

    public void inserir(Fornecedor f) {
        String sql = "INSERT INTO fornecedor (nome_fantasia, cnpj, telefone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencher(stmt, f);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar fornecedor: " + traduzirErro(e));
        }
    }

    public List<Fornecedor> listar() {
        String sql = "SELECT id, nome_fantasia, cnpj, telefone, email FROM fornecedor ORDER BY nome_fantasia";
        List<Fornecedor> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Fornecedor(rs.getInt("id"), rs.getString("nome_fantasia"),
                        rs.getString("cnpj"), rs.getString("telefone"), rs.getString("email")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar fornecedores: " + e.getMessage());
        }
        return lista;
    }

    public void atualizar(Fornecedor f) {
        String sql = "UPDATE fornecedor SET nome_fantasia = ?, cnpj = ?, telefone = ?, email = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            preencher(stmt, f);
            stmt.setInt(5, f.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar fornecedor: " + traduzirErro(e));
        }
    }

    
    public void excluir(int id) {
        String sql = "DELETE FROM fornecedor WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir fornecedor: " + e.getMessage());
        }
    }

    public int contarPecas(int fornecedorId) {
        String sql = "SELECT COUNT(*) FROM peca WHERE fornecedor_id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fornecedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar peças: " + e.getMessage());
        }
    }

    private void preencher(PreparedStatement stmt, Fornecedor f) throws SQLException {
        stmt.setString(1, f.getNomeFantasia());
        stmt.setString(2, vazioParaNull(f.getCnpj()));   // CNPJ é único: vazio vira NULL
        stmt.setString(3, vazioParaNull(f.getTelefone()));
        stmt.setString(4, vazioParaNull(f.getEmail()));
    }

    private static String vazioParaNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private String traduzirErro(SQLException e) {
        if (e.getErrorCode() == 1062) return "já existe um fornecedor com esse CNPJ.";
        return e.getMessage();
    }
}
