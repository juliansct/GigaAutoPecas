package gigapecas.view;

import gigapecas.dao.FuncionarioDAO;
import gigapecas.model.Funcionario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Supplier;

public class TelaInicio extends JFrame {

    private final String usuario;
    private final boolean gerente;
    private final String primeiroNome;

    public TelaInicio(String usuario) {
        this.usuario = usuario;
        Funcionario eu = buscar(usuario);
        this.gerente = eu != null && eu.isGerente();
        this.primeiroNome = eu == null || eu.getNome() == null || eu.getNome().isBlank()
                ? usuario : eu.getNome().trim().split("\\s+")[0];
        setTitle("GigaAutoPeças - Início");
        setSize(1100, 720);
        setMinimumSize(new Dimension(820, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton btnSair = Estilo.botao("Sair", new Color(51, 65, 95), new Color(71, 85, 115));
        btnSair.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Deseja sair do sistema?", "Sair", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                dispose();
                new TelaLogin().setVisible(true);
            }
        });

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        JButton btnMinhaConta = Estilo.botao("Minha conta", new Color(51, 65, 95), new Color(71, 85, 115));
        btnMinhaConta.addActionListener(e -> {
            switch (DialogoMinhaConta.mostrar(this, usuario)) {
                case LOGIN_ALTERADO -> { dispose(); new TelaLogin().setVisible(true); }   // entra de novo com o login novo
                case DADOS_ALTERADOS -> { dispose(); new TelaInicio(usuario).setVisible(true); } // mostra o nome novo
                default -> { }
            }
        });

        raiz.add(Estilo.cabecalho("Início", primeiroNome, btnMinhaConta, btnSair), BorderLayout.NORTH);

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setOpaque(false);
        conteudo.setBorder(BorderFactory.createEmptyBorder(22, 32, 24, 32));

        JLabel ola = new JLabel("Olá, " + primeiroNome + "! O que você quer fazer hoje?");
        ola.setFont(Estilo.fonte(Font.BOLD, 22));
        ola.setForeground(Estilo.TEXTO);
        ola.setAlignmentX(LEFT_ALIGNMENT);
        conteudo.add(ola);
        conteudo.add(Box.createVerticalStrut(4));
        JLabel sub = new JLabel("Escolha um módulo abaixo.");
        sub.setFont(Estilo.fonte(Font.PLAIN, 13));
        sub.setForeground(Estilo.TEXTO_SUAVE);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        conteudo.add(sub);

        conteudo.add(Box.createVerticalStrut(8));
        JPanel colunas = new JPanel(new GridLayout(1, 3, 24, 0));
        colunas.setOpaque(false);
        colunas.setAlignmentX(LEFT_ALIGNMENT);

        
        colunas.add(secao("VENDAS",
                bloco("Nova Venda", "Caixa: vender peças e baixar o estoque", Icones.Tipo.CARRINHO, Estilo.VERDE,
                        () -> new TelaVenda(usuario)),
                bloco("Relatório de Vendas", "Faturamento, lucro e mais vendidas", Icones.Tipo.GRAFICO,
                        new Color(147, 51, 234), () -> new TelaRelatorioVendas(usuario))));

        colunas.add(secao("PRODUTOS",
                bloco("Peças", "Cadastro, reposição e histórico de estoque", Icones.Tipo.CAIXA, Estilo.AZUL,
                        () -> new TelaPecas(usuario)),
                bloco("Categorias", "Classificação das peças (Freios, Filtros...)", Icones.Tipo.ETIQUETA, new Color(219, 39, 119),
                        TelaCategorias::new),
                bloco("Fornecedores", "De quem a loja compra as peças", Icones.Tipo.CAMINHAO, Estilo.LARANJA,
                        TelaFornecedores::new)));

        colunas.add(secao("ADMINISTRAÇÃO",
                bloco("Funcionários", "Cadastro de usuários e senhas", Icones.Tipo.PESSOAS, new Color(13, 148, 136),
                        gerente ? () -> new TelaFuncionarios(usuario) : null, "Só gerente")));

        colunas.setMaximumSize(new Dimension(Integer.MAX_VALUE, colunas.getPreferredSize().height));
        conteudo.add(colunas);

        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Estilo.FUNDO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        raiz.add(scroll, BorderLayout.CENTER);

        setContentPane(raiz);
    }

   
    private JPanel secao(String titulo, JComponent... blocos) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(Estilo.fonte(Font.BOLD, 12));
        lbl.setForeground(Estilo.TEXTO_SUAVE);
        p.add(lbl, BorderLayout.NORTH);

        
        JPanel pilha = new JPanel(new GridLayout(3, 1, 0, 14));
        pilha.setOpaque(false);
        for (JComponent b : blocos) pilha.add(b);
        for (int i = blocos.length; i < 3; i++) {
            JPanel vazio = new JPanel();
            vazio.setOpaque(false);
            pilha.add(vazio);
        }
        p.add(pilha, BorderLayout.CENTER);
        return p;
    }

    
    private static Funcionario buscar(String usuario) {
        try {
            return new FuncionarioDAO().buscarPorUsuario(usuario);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private JComponent bloco(String titulo, String descricao, Icones.Tipo icone, Color cor,
                             Supplier<JFrame> abrirTela) {
        return bloco(titulo, descricao, icone, cor, abrirTela, "Em breve");
    }

   
    private JComponent bloco(String titulo, String descricao, Icones.Tipo icone, Color cor,
                             Supplier<JFrame> abrirTela, String seloDesativado) {
        boolean ativo = abrirTela != null;
        Estilo.Cartao card = new Estilo.Cartao(new BorderLayout(), 16);
        card.setPreferredSize(new Dimension(280, 138));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        // círculo colorido com o ícone
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        Color corIcone = ativo ? cor : new Color(148, 163, 184);
        JLabel lblIcone = new JLabel(new Icones(icone, 24, corIcone)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(corIcone.getRed(), corIcone.getGreen(), corIcone.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcone.setPreferredSize(new Dimension(46, 46));
        topo.add(lblIcone, BorderLayout.WEST);

        if (!ativo) {
            JLabel selo = new JLabel(seloDesativado);
            selo.setFont(Estilo.fonte(Font.BOLD, 11));
            selo.setForeground(Estilo.TEXTO_SUAVE);
            selo.setOpaque(true);
            selo.setBackground(new Color(226, 232, 240));
            selo.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            JPanel caixaSelo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            caixaSelo.setOpaque(false);
            caixaSelo.add(selo);
            topo.add(caixaSelo, BorderLayout.EAST);
        }
        card.add(topo, BorderLayout.NORTH);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilo.fonte(Font.BOLD, 17));
        lblTitulo.setForeground(ativo ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
        JLabel lblDesc = new JLabel(descricao);
        lblDesc.setFont(Estilo.fonte(Font.PLAIN, 12));
        lblDesc.setForeground(Estilo.TEXTO_SUAVE);
        textos.add(lblTitulo);
        textos.add(lblDesc);
        card.add(textos, BorderLayout.SOUTH);

        if (ativo) {
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { card.setCorBorda(cor); }
                @Override public void mouseExited(MouseEvent e)  { card.setCorBorda(Estilo.BORDA); }
                @Override public void mouseClicked(MouseEvent e) {
                    card.setCorBorda(Estilo.BORDA);
                    abrir(abrirTela.get());
                }
            });
        } else {
            card.setBackground(new Color(250, 251, 253));
        }
        return card;
    }

   
    private void abrir(JFrame tela) {
        tela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tela.setLocationRelativeTo(this);
        tela.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { setVisible(true); }
        });
        setVisible(false);
        tela.setVisible(true);
    }
}
