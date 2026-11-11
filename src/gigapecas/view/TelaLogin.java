package gigapecas.view;

import gigapecas.dao.FuncionarioDAO;
import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtSenha;
    private FuncionarioDAO funcionarioDAO;

    public TelaLogin() {
        funcionarioDAO = new FuncionarioDAO();
        funcionarioDAO.verificarEInserirAdminPadrao();

        setTitle("GigaAutoPeças - Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());

     
        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

       
        JLabel lblTitulo = new JLabel("GigaAutoPeças", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 44, 77)); 
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0); 
        painelPrincipal.add(lblTitulo, gbc);

       
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 1;

        
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        painelPrincipal.add(lblUsuario, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        txtUsuario = new JTextField(15);
        txtUsuario.setPreferredSize(new Dimension(180, 28));
        painelPrincipal.add(txtUsuario, gbc);

        
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(new Font("Arial", Font.PLAIN, 14));
        painelPrincipal.add(lblSenha, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        txtSenha = new JPasswordField(15);
        txtSenha.setPreferredSize(new Dimension(180, 28));
        painelPrincipal.add(txtSenha, gbc);

       
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 0, 0, 0); 
        
        JButton btnEntrar = new JButton("Entrar no Sistema");
        btnEntrar.setPreferredSize(new Dimension(180, 35));
        btnEntrar.setFont(new Font("Arial", Font.BOLD, 13));
        painelPrincipal.add(btnEntrar, gbc);

        
        add(painelPrincipal, BorderLayout.CENTER);

        
        btnEntrar.addActionListener(e -> {
            String usuario = txtUsuario.getText();
            String senha = new String(txtSenha.getPassword());

            if (usuario.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean autorizado = funcionarioDAO.validarLogin(usuario, senha);

            if (autorizado) {
                JOptionPane.showMessageDialog(this, "Bem-vindo à GigaAutoPeças!");
                new TelaPrincipal().setVisible(true);
                this.dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!", "Erro de Acesso", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}