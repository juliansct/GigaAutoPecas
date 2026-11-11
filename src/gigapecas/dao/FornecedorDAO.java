package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import gigapecas.model.Fornecedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDAO {

    public void inserir(Fornecedor fornecedor) {
        String sql = "INSERT INTO fornecedor (nome_fantasia, cnpj, telefone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fornecedor.getNomeFantasia());
            stmt.setString(2, fornecedor.getCnpj());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir fornecedor: " + e.getMessage());
        }
    }

    public List<Fornecedor> listar() {
        String sql = "SELECT * FROM fornecedor";
        List<Fornecedor> fornecedores = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Fornecedor f = new Fornecedor(
                    rs.getInt("id"),
                    rs.getString("nome_fantasia"),
                    rs.getString("cnpj"),
                    rs.getString("telefone"),
                    rs.getString("email")
                );
                fornecedores.add(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar fornecedores: " + e.getMessage());
        }
        return fornecedores;
    }
}