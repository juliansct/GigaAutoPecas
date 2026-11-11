package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import gigapecas.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public void inserir(Categoria categoria) {
        String sql = "INSERT INTO categoria (nome, descricao) VALUES (?, ?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir categoria: " + e.getMessage());
        }
    }

    public List<Categoria> listar() {
        String sql = "SELECT * FROM categoria";
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("descricao")
                );
                categorias.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar categorias: " + e.getMessage());
        }
        return categorias;
    }
}