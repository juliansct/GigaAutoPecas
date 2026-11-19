package gigapecas.dao;

import com.gigapecas.conexao.Conexao;
import com.gigapecas.util.CriptografiaUtil;
import java.io.FileInputStream;
import java.io.IOException;
import gigapecas.model.Funcionario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FuncionarioDAO {

    public void verificarEInserirAdminPadrao() {
        String sqlCheck = "SELECT COUNT(*) FROM funcionario";
        String sqlInsert = "INSERT INTO funcionario (nome, usuario, senha, cargo, precisa_trocar_senha) VALUES (?, ?, ?, ?, TRUE)";
        
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

    public List<Funcionario> listar() {
        String sql = "SELECT id, nome, usuario, cargo, precisa_trocar_senha FROM funcionario ORDER BY nome";
        List<Funcionario> lista = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Funcionario f = new Funcionario(rs.getInt("id"), rs.getString("nome"),
                        rs.getString("usuario"), rs.getString("cargo"));
                f.setPrecisaTrocarSenha(rs.getBoolean("precisa_trocar_senha"));
                lista.add(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar funcionários: " + e.getMessage());
        }
        return lista;
    }

   
    public Funcionario buscarPorUsuario(String usuario) {
        String sql = "SELECT id, nome, usuario, cargo, precisa_trocar_senha FROM funcionario WHERE usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                Funcionario f = new Funcionario(rs.getInt("id"), rs.getString("nome"),
                        rs.getString("usuario"), rs.getString("cargo"));
                f.setPrecisaTrocarSenha(rs.getBoolean("precisa_trocar_senha"));
                return f;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar funcionário: " + e.getMessage());
        }
    }

    
    public boolean atualizarPropriaConta(String usuarioAtual, String senhaAtual, String novoNome, String novoUsuario) {
        if (!validarLogin(usuarioAtual, senhaAtual)) return false;
        String sql = "UPDATE funcionario SET nome = ?, usuario = ? WHERE usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoNome);
            stmt.setString(2, novoUsuario);
            stmt.setString(3, usuarioAtual);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("Já existe um funcionário com o usuário \"" + novoUsuario + "\".");
            throw new RuntimeException("Erro ao atualizar conta: " + e.getMessage());
        }
    }

    
    public void atualizarPeloGerente(int id, String nome, String cargo) {
        String sql = "UPDATE funcionario SET nome = ?, cargo = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, cargo);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar funcionário: " + e.getMessage());
        }
    }

    public boolean usuarioExiste(String usuario) {
        String sql = "SELECT 1 FROM funcionario WHERE usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar usuário: " + e.getMessage());
        }
    }

   
    public void inserir(Funcionario f, String senhaProvisoria) {
        String sql = "INSERT INTO funcionario (nome, usuario, senha, cargo, precisa_trocar_senha) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, f.getNome());
            stmt.setString(2, f.getUsuario());
            stmt.setString(3, CriptografiaUtil.senhaParaHash(senhaProvisoria));
            stmt.setString(4, f.getCargo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar funcionário: " + e.getMessage());
        }
    }

   
    public void definirSenhaProvisoria(int id, String senhaProvisoria) {
        String sql = "UPDATE funcionario SET senha = ?, precisa_trocar_senha = TRUE WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, CriptografiaUtil.senhaParaHash(senhaProvisoria));
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao redefinir senha: " + e.getMessage());
        }
    }

  
    public boolean trocarPropriaSenha(String usuario, String senhaAtual, String novaSenha) {
        if (!validarLogin(usuario, senhaAtual)) return false;
        String sql = "UPDATE funcionario SET senha = ?, precisa_trocar_senha = FALSE WHERE usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, CriptografiaUtil.senhaParaHash(novaSenha));
            stmt.setString(2, usuario);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao trocar senha: " + e.getMessage());
        }
    }

    
    public boolean precisaTrocarSenha(String usuario) {
        String sql = "SELECT precisa_trocar_senha FROM funcionario WHERE usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar senha: " + e.getMessage());
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM funcionario WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir funcionário: " + e.getMessage());
        }
    }
}
