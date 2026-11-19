package gigapecas.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;


public class CampoData extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final JTextField campo = Estilo.estilizarCampo(new JTextField(8));
    private final JPopupMenu popup = new JPopupMenu();
    private YearMonth mesVisivel = YearMonth.now();
    private Runnable aoMudar = () -> {};

    public CampoData(LocalDate inicial) {
        super(new BorderLayout(4, 0));
        setOpaque(false);
        add(campo, BorderLayout.CENTER);

        JButton btn = Estilo.botaoSecundario("");
        btn.setIcon(new IconeCalendario());
        btn.setToolTipText("Escolher no calendário");
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        btn.addActionListener(e -> abrirCalendario());
        add(btn, BorderLayout.EAST);

        campo.setToolTipText("dd/mm/aaaa");
        campo.addActionListener(e -> aoMudar.run());   // Enter no campo
        setData(inicial);
    }

 
    public void aoMudar(Runnable acao) { this.aoMudar = acao; }

    public void setData(LocalDate data) {
        campo.setText(data == null ? "" : data.format(FORMATO));
    }

    
    public LocalDate getData() {
        try {
            return LocalDate.parse(campo.getText().trim(), FORMATO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

   
    private void abrirCalendario() {
        LocalDate atual = getData();
        mesVisivel = YearMonth.from(atual != null ? atual : LocalDate.now());
        montarCalendario();
        popup.show(this, 0, getHeight() + 2);
    }

    private void montarCalendario() {
        popup.removeAll();
        popup.setBorder(BorderFactory.createLineBorder(Estilo.BORDA));
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBackground(Estilo.BRANCO);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

       
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        JButton ant = botaoNav("‹");
        JButton prox = botaoNav("›");
        String nomeMes = mesVisivel.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
        JLabel titulo = new JLabel(Character.toUpperCase(nomeMes.charAt(0)) + nomeMes.substring(1) + " " + mesVisivel.getYear(),
                SwingConstants.CENTER);
        titulo.setFont(Estilo.fonte(Font.BOLD, 14));
        titulo.setForeground(Estilo.TEXTO);
        nav.add(ant, BorderLayout.WEST);
        nav.add(titulo, BorderLayout.CENTER);
        nav.add(prox, BorderLayout.EAST);
        ant.addActionListener(e -> { mesVisivel = mesVisivel.minusMonths(1); montarCalendario(); });
        prox.addActionListener(e -> { mesVisivel = mesVisivel.plusMonths(1); montarCalendario(); });
        painel.add(nav, BorderLayout.NORTH);

       
        JPanel grade = new JPanel(new GridLayout(0, 7, 2, 2));
        grade.setOpaque(false);
        for (String d : new String[]{"D", "S", "T", "Q", "Q", "S", "S"}) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(Estilo.fonte(Font.BOLD, 11));
            l.setForeground(Estilo.TEXTO_SUAVE);
            grade.add(l);
        }
        LocalDate primeiro = mesVisivel.atDay(1);
        int vazios = primeiro.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : primeiro.getDayOfWeek().getValue();
        for (int i = 0; i < vazios; i++) grade.add(new JLabel());

        LocalDate hoje = LocalDate.now();
        LocalDate selecionada = getData();
        for (int dia = 1; dia <= mesVisivel.lengthOfMonth(); dia++) {
            LocalDate data = mesVisivel.atDay(dia);
            grade.add(botaoDia(data, data.equals(selecionada), data.equals(hoje)));
        }
        painel.add(grade, BorderLayout.CENTER);

        JButton btnHoje = Estilo.botaoSecundario("Hoje");
        btnHoje.addActionListener(e -> escolher(LocalDate.now()));
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        rodape.setOpaque(false);
        rodape.add(btnHoje);
        painel.add(rodape, BorderLayout.SOUTH);

        popup.add(painel);
        popup.pack();
        popup.revalidate();
        popup.repaint();
    }

    private JButton botaoNav(String texto) {
        JButton b = new JButton(texto);
        b.setFont(Estilo.fonte(Font.BOLD, 18));
        b.setForeground(Estilo.TEXTO);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton botaoDia(LocalDate data, boolean selecionado, boolean hoje) {
        JButton b = new JButton(String.valueOf(data.getDayOfMonth())) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selecionado) {
                    g2.setColor(Estilo.AZUL);
                    g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 8, 8));
                } else if (getModel().isRollover()) {
                    g2.setColor(Estilo.SELECAO);
                    g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 8, 8));
                } else if (hoje) {
                    g2.setColor(Estilo.AZUL);
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(Estilo.fonte(selecionado || hoje ? Font.BOLD : Font.PLAIN, 12));
        b.setForeground(selecionado ? Color.WHITE : Estilo.TEXTO);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(34, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> escolher(data));
        return b;
    }

    private void escolher(LocalDate data) {
        setData(data);
        popup.setVisible(false);
        aoMudar.run();
    }

    /** Ícone de calendário desenhado em código. */
    private static class IconeCalendario implements Icon {
        @Override public int getIconWidth()  { return 16; }
        @Override public int getIconHeight() { return 16; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(Estilo.TEXTO);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 3, 14, 12, 3, 3);
            g2.drawLine(1, 7, 15, 7);
            g2.drawLine(5, 1, 5, 4);
            g2.drawLine(11, 1, 11, 4);
            g2.fillRect(4, 9, 2, 2);
            g2.fillRect(7, 9, 2, 2);
            g2.fillRect(10, 9, 2, 2);
            g2.fillRect(4, 12, 2, 2);
            g2.dispose();
        }
    }
}
