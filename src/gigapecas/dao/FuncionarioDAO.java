package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import com.gigapecas.util.CriptografiaUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class FuncionarioDAO {

    public void verificarEInserirAdminPadrao() {
        String sqlCheck = "SELECT COUNT(*) FROM funcionario";
        String sqlInsert = "INSERT INTO funcionario (nome, usuario, senha, cargo) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck);
             ResultSet rs = stmtCheck.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream("config.properties")) {
                    props.load(fis);
                }
                String senhaPura = props.getProperty("admin.senha.inicial", "1234");
                String senhaHash = CriptografiaUtil.senhaParaHash(senhaPura);
                
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                    stmtInsert.setString(1, "Administrador");
                    stmtInsert.setString(2, "admin");
                    stmtInsert.setString(3, senhaHash);
                    stmtInsert.setString(4, "Gerente de Estoque");
                    stmtInsert.executeUpdate();
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Erro ao configurar admin padrão: " + e.getMessage());
        }
    }

    public boolean validarLogin(String usuario, String senhaPura) {
        String senhaHash = CriptografiaUtil.senhaParaHash(senhaPura);
        String sql = "SELECT * FROM funcionario WHERE usuario = ? AND senha = ?";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario);
            stmt.setString(2, senhaHash);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar login: " + e.getMessage());
        }
    }
}