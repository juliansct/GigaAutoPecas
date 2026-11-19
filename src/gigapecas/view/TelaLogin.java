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
        setSize(780, 460);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(criarPainelMarca());
        raiz.add(criarPainelFormulario());
        setContentPane(raiz);
    }

   
    private JPanel criarPainelMarca() {
        JPanel painel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Estilo.AZUL_ESCURO, 0, getHeight(), new Color(30, 58, 110)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // círculos decorativos
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillOval(-80, getHeight() - 180, 260, 260);
                g2.fillOval(getWidth() - 120, -60, 200, 200);
                g2.dispose();
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblIcone = new JLabel(new Icones.Engrenagem(56, Estilo.LARANJA));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 18, 0);
        painel.add(lblIcone, gbc);

        JLabel lblMarca = new JLabel("<html>GIGA<span style='color:#F97316'>AUTO</span>PEÇAS</html>");
        lblMarca.setFont(Estilo.fonte(Font.BOLD, 28));
        lblMarca.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        painel.add(lblMarca, gbc);

        JLabel lblSlogan = new JLabel("<html>Controle de estoque de autopeças<br>simples e rápido.</html>");
        lblSlogan.setFont(Estilo.fonte(Font.PLAIN, 14));
        lblSlogan.setForeground(new Color(203, 213, 225));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        painel.add(lblSlogan, gbc);

        return painel;
    }

    
    private JPanel criarPainelFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Estilo.BRANCO);
        painel.setBorder(BorderFactory.createEmptyBorder(0, 48, 0, 48));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblTitulo = new JLabel("Bem-vindo");
        lblTitulo.setFont(Estilo.fonte(Font.BOLD, 22));
        lblTitulo.setForeground(Estilo.TEXTO);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        painel.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("Entre com seu usuário e senha");
        lblSub.setFont(Estilo.fonte(Font.PLAIN, 13));
        lblSub.setForeground(Estilo.TEXTO_SUAVE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 26, 0);
        painel.add(lblSub, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        painel.add(Estilo.rotulo("USUÁRIO"), gbc);

        txtUsuario = Estilo.estilizarCampo(new JTextField(18));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 16, 0);
        painel.add(txtUsuario, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        painel.add(Estilo.rotulo("SENHA"), gbc);

        txtSenha = Estilo.estilizarCampo(new JPasswordField(18));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 26, 0);
        painel.add(Estilo.comOlho(txtSenha), gbc);

        JButton btnEntrar = Estilo.botao("Entrar no Sistema", Estilo.AZUL, Estilo.AZUL_HOVER);
        btnEntrar.setFont(Estilo.fonte(Font.BOLD, 14));
        btnEntrar.setPreferredSize(new Dimension(10, 44));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        painel.add(btnEntrar, gbc);

        
        getRootPane().setDefaultButton(btnEntrar);
        btnEntrar.addActionListener(e -> fazerLogin());

        return painel;
    }

    private void fazerLogin() {
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean autorizado;
        boolean precisaTrocar;
        try {
            autorizado = funcionarioDAO.validarLogin(usuario, senha);
            precisaTrocar = autorizado && funcionarioDAO.precisaTrocarSenha(usuario);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (autorizado) {
            
            if (precisaTrocar && !DialogoTrocarSenha.mostrar(this, usuario, senha, true)) {
                txtSenha.setText("");
                return;
            }
            new TelaInicio(usuario).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!", "Erro de Acesso", JOptionPane.ERROR_MESSAGE);
            txtSenha.setText("");
            txtSenha.requestFocusInWindow();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarTema();
            new TelaLogin().setVisible(true);
        });
    }
}
