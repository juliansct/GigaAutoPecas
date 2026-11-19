package gigapecas.view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;


public final class Estilo {

    private Estilo() {}

    // ---------- Paleta ----------
    public static final Color AZUL_ESCURO   = new Color(22, 33, 58);    
    public static final Color AZUL          = new Color(37, 99, 235);   
    public static final Color AZUL_HOVER    = new Color(29, 78, 216);
    public static final Color LARANJA       = new Color(249, 115, 22);  
    public static final Color VERDE         = new Color(22, 163, 74);
    public static final Color VERDE_HOVER   = new Color(21, 128, 61);
    public static final Color VERMELHO      = new Color(220, 38, 38);
    public static final Color VERMELHO_HOVER= new Color(185, 28, 28);
    public static final Color FUNDO         = new Color(243, 245, 249);
    public static final Color BRANCO        = Color.WHITE;
    public static final Color BORDA         = new Color(214, 219, 228);
    public static final Color TEXTO         = new Color(30, 41, 59);
    public static final Color TEXTO_SUAVE   = new Color(100, 116, 139);
    public static final Color LINHA_ALTERNADA = new Color(248, 250, 252);
    public static final Color SELECAO       = new Color(219, 234, 254);
    public static final Color ALERTA_FUNDO  = new Color(254, 226, 226);

   
    public static final java.text.NumberFormat MOEDA =
            java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("pt-BR"));

   
    private static final String FAMILIA = escolherFamilia();

    public static Font fonte(int estilo, int tamanho) {
        return new Font(FAMILIA, estilo, tamanho);
    }

    private static String escolherFamilia() {
        String[] instaladas = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String preferida : new String[]{"Segoe UI", "Inter", "Roboto", "Helvetica Neue", "Arial"}) {
            for (String f : instaladas) {
                if (f.equalsIgnoreCase(preferida)) return f;
            }
        }
        return Font.SANS_SERIF;
    }

   
    public static void aplicarTema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            
        }
        Font base = fonte(Font.PLAIN, 13);
        UIManager.put("OptionPane.messageFont", base);
        UIManager.put("OptionPane.buttonFont", base);
        UIManager.put("Label.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("PasswordField.font", base);
        UIManager.put("Menu.font", base);
        UIManager.put("MenuItem.font", base);
        UIManager.put("ToolTip.font", fonte(Font.PLAIN, 12));
    }

    
    private static final Border BORDA_CAMPO = BorderFactory.createCompoundBorder(
            new BordaArredondada(BORDA, 8), BorderFactory.createEmptyBorder(6, 10, 6, 10));
    private static final Border BORDA_CAMPO_FOCO = BorderFactory.createCompoundBorder(
            new BordaArredondada(AZUL, 8), BorderFactory.createEmptyBorder(6, 10, 6, 10));

    public static <T extends JTextField> T estilizarCampo(T campo) {
        campo.setFont(fonte(Font.PLAIN, 14));
        campo.setForeground(TEXTO);
        campo.setBackground(BRANCO);
        campo.setCaretColor(AZUL);
        campo.setBorder(BORDA_CAMPO);
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, 40));
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { campo.setBorder(BORDA_CAMPO_FOCO); }
            @Override public void focusLost(FocusEvent e)   { campo.setBorder(BORDA_CAMPO); }
        });
        return campo;
    }

   
    public static JComponent comOlho(JPasswordField campo) {
        char ocultar = campo.getEchoChar();
        JToggleButton olho = new JToggleButton(new Icones(Icones.Tipo.OLHO, 18, TEXTO_SUAVE));
        olho.setSelectedIcon(new Icones(Icones.Tipo.OLHO_RISCADO, 18, AZUL));
        olho.setToolTipText("Mostrar senha");
        olho.setFocusable(false);           // Tab continua indo de campo em campo
        olho.setContentAreaFilled(false);
        olho.setBorder(BorderFactory.createCompoundBorder(
                new BordaArredondada(BORDA, 8), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        olho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        olho.addActionListener(e -> {
            boolean mostrar = olho.isSelected();
            campo.setEchoChar(mostrar ? (char) 0 : ocultar);
            olho.setToolTipText(mostrar ? "Ocultar senha" : "Mostrar senha");
            campo.requestFocusInWindow();
        });

        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(campo, BorderLayout.CENTER);
        p.add(olho, BorderLayout.EAST);
        return p;
    }

    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(Font.BOLD, 12));
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

   
    public static JButton botao(String texto, Color cor, Color corHover) {
        return new BotaoArredondado(texto, cor, corHover, BRANCO);
    }


    public static JButton botaoSecundario(String texto) {
        BotaoArredondado b = new BotaoArredondado(texto, BRANCO, new Color(241, 245, 249), TEXTO);
        b.corBorda = BORDA;
        return b;
    }

    static class BotaoArredondado extends JButton {
        private final Color cor, corHover;
        Color corBorda;
        private boolean emCima;

        BotaoArredondado(String texto, Color cor, Color corHover, Color corTexto) {
            super(texto);
            this.cor = cor;
            this.corHover = corHover;
            setFont(fonte(Font.BOLD, 13));
            setForeground(corTexto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { emCima = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { emCima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fundo = emCima || getModel().isPressed() ? corHover : cor;
            if (!isEnabled()) fundo = new Color(203, 213, 225);
            g2.setColor(fundo);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
            if (corBorda != null) {
                g2.setColor(corBorda);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    public static class Cartao extends JPanel {
        private final int raio;
        private Color corBorda = BORDA;

        public void setCorBorda(Color cor) { this.corBorda = cor; repaint(); }

        public Cartao(LayoutManager layout, int raio) {
            super(layout);
            this.raio = raio;
            setOpaque(false);
            setBackground(BRANCO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, raio, raio));
            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(corBorda == BORDA ? 1f : 2f));
            g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, raio, raio));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    public static JPanel cabecalho(String secao, String usuario, JButton... botoesDireita) {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(AZUL_ESCURO);
        cab.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        esquerda.setOpaque(false);
        esquerda.add(new JLabel(new Icones.Engrenagem(28, LARANJA)));
        JLabel marca = new JLabel("<html>GIGA<span style='color:#F97316'>AUTO</span>PEÇAS</html>");
        marca.setFont(fonte(Font.BOLD, 18));
        marca.setForeground(Color.WHITE);
        esquerda.add(marca);
        JLabel lblSecao = new JLabel("  |  " + secao);
        lblSecao.setFont(fonte(Font.PLAIN, 14));
        lblSecao.setForeground(new Color(148, 163, 184));
        esquerda.add(lblSecao);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        direita.setOpaque(false);
        if (usuario != null) {
            JLabel ola = new JLabel("Olá, " + usuario);
            ola.setFont(fonte(Font.PLAIN, 13));
            ola.setForeground(new Color(203, 213, 225));
            direita.add(ola);
        }
        for (JButton b : botoesDireita) direita.add(b);

        cab.add(esquerda, BorderLayout.WEST);
        cab.add(direita, BorderLayout.EAST);
        return cab;
    }

   
    public static JButton botaoVoltar(Window janela) {
        JButton b = botao("Início", new Color(51, 65, 95), new Color(71, 85, 115));
        b.addActionListener(e -> janela.dispose());
        return b;
    }

    // ---------- Tabela padrão ----------
    public static void estilizarTabela(JTable tabela) {
        tabela.setRowHeight(34);
        tabela.setFont(fonte(Font.PLAIN, 13));
        tabela.setForeground(TEXTO);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(new Color(235, 238, 243));
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(BRANCO);
        tabela.setSelectionBackground(SELECAO);
        tabela.setSelectionForeground(TEXTO);

        javax.swing.table.JTableHeader header = tabela.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
                l.setFont(fonte(Font.BOLD, 12));
                l.setForeground(TEXTO_SUAVE);
                l.setBackground(new Color(241, 245, 249));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                l.setText(v == null ? "" : v.toString().toUpperCase());
                
                Class<?> tipo = t.getColumnClass(c);
                l.setHorizontalAlignment(Number.class.isAssignableFrom(tipo) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return l;
            }
        });
        tabela.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, false, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!s) l.setBackground(r % 2 == 0 ? BRANCO : LINHA_ALTERNADA);
                return l;
            }
        });
    }

    static class BordaArredondada implements Border {
        private final Color cor;
        private final int raio;

        BordaArredondada(Color cor, int raio) { this.cor = cor; this.raio = raio; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 1, raio, raio));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    
    public static JPanel cartaoResumo(String titulo, JLabel valor, Color destaque) {
        Cartao c = new Cartao(new BorderLayout(), 14);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(14, 16, 14, 16),
                BorderFactory.createMatteBorder(0, 4, 0, 0, destaque)));
        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);
        textos.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(fonte(Font.PLAIN, 12));
        lbl.setForeground(TEXTO_SUAVE);
        valor.setFont(fonte(Font.BOLD, 22));
        valor.setForeground(TEXTO);
        textos.add(lbl, BorderLayout.NORTH);
        textos.add(valor, BorderLayout.CENTER);
        c.add(textos, BorderLayout.CENTER);
        return c;
    }

   
    public static JScrollPane rolagem(JTable tabela) {
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(BORDA));
        scroll.getViewport().setBackground(BRANCO);
        return scroll;
    }

    
    public static JLabel titulo(String texto, int tamanho) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(Font.BOLD, tamanho));
        l.setForeground(TEXTO);
        return l;
    }


    public static javax.swing.table.DefaultTableCellRenderer renderMoeda() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, false, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                if (v instanceof Number) l.setText(MOEDA.format(((Number) v).doubleValue()));
                if (!s) l.setBackground(r % 2 == 0 ? BRANCO : LINHA_ALTERNADA);
                return l;
            }
        };
    }

    
    public static javax.swing.table.DefaultTableCellRenderer renderNumero() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, false, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                if (!s) l.setBackground(r % 2 == 0 ? BRANCO : LINHA_ALTERNADA);
                return l;
            }
        };
    }
}
