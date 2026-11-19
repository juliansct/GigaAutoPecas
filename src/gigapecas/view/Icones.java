package gigapecas.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;


public class Icones implements Icon {

    public enum Tipo { CAIXA, SETAS, CAMINHAO, ETIQUETA, PESSOAS, GRAFICO, CARRINHO, OLHO, OLHO_RISCADO }

    private final Tipo tipo;
    private final int tam;
    private final Color cor;

    public Icones(Tipo tipo, int tam, Color cor) {
        this.tipo = tipo;
        this.tam = tam;
        this.cor = cor;
    }

    @Override public int getIconWidth()  { return tam; }
    @Override public int getIconHeight() { return tam; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.scale(tam / 24.0, tam / 24.0);          // desenha numa grade de 24x24
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (tipo) {
            case CAIXA -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 2); p.lineTo(21, 7); p.lineTo(21, 17); p.lineTo(12, 22);
                p.lineTo(3, 17); p.lineTo(3, 7); p.closePath();
                g2.draw(p);
                g2.draw(new Line2D.Double(3, 7, 12, 12));
                g2.draw(new Line2D.Double(21, 7, 12, 12));
                g2.draw(new Line2D.Double(12, 12, 12, 22));
            }
            case SETAS -> {
                g2.draw(new Line2D.Double(4, 8, 20, 8));
                g2.draw(new Line2D.Double(16, 4, 20, 8));
                g2.draw(new Line2D.Double(16, 12, 20, 8));
                g2.draw(new Line2D.Double(20, 16, 4, 16));
                g2.draw(new Line2D.Double(8, 12, 4, 16));
                g2.draw(new Line2D.Double(8, 20, 4, 16));
            }
            case CAMINHAO -> {
                g2.draw(new RoundRectangle2D.Double(2, 6, 12, 10, 2, 2));
                Path2D p = new Path2D.Double();
                p.moveTo(14, 9); p.lineTo(18, 9); p.lineTo(22, 13); p.lineTo(22, 16); p.lineTo(14, 16);
                g2.draw(p);
                g2.fill(new Ellipse2D.Double(4.5, 15.5, 4, 4));
                g2.fill(new Ellipse2D.Double(15.5, 15.5, 4, 4));
            }
            case ETIQUETA -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 3); p.lineTo(12, 3); p.lineTo(21, 12); p.lineTo(12, 21); p.lineTo(3, 12); p.closePath();
                g2.draw(p);
                g2.fill(new Ellipse2D.Double(6, 6, 3.5, 3.5));
            }
            case PESSOAS -> {
                g2.draw(new Ellipse2D.Double(5, 3, 7, 7));
                g2.draw(new Arc2D.Double(2, 13, 13, 12, 0, 180, Arc2D.OPEN));
                g2.draw(new Arc2D.Double(14, 4, 6, 6, -60, 240, Arc2D.OPEN));
                g2.draw(new Arc2D.Double(15, 13, 8, 10, 0, 90, Arc2D.OPEN));
            }
            case CARRINHO -> {
                Path2D p = new Path2D.Double();
                p.moveTo(2, 3); p.lineTo(5, 3); p.lineTo(7.5, 15); p.lineTo(19, 15); p.lineTo(21, 7); p.lineTo(6, 7);
                g2.draw(p);
                g2.fill(new Ellipse2D.Double(7.5, 17.5, 3.5, 3.5));
                g2.fill(new Ellipse2D.Double(15.5, 17.5, 3.5, 3.5));
            }
            case OLHO, OLHO_RISCADO -> {
                Path2D p = new Path2D.Double();
                p.moveTo(2, 12); p.quadTo(12, 2, 22, 12); p.quadTo(12, 22, 2, 12); p.closePath();
                g2.draw(p);
                g2.draw(new Ellipse2D.Double(8.5, 8.5, 7, 7));
                if (tipo == Tipo.OLHO_RISCADO) g2.draw(new Line2D.Double(4, 20, 20, 4));
            }
            case GRAFICO -> {
                g2.draw(new Line2D.Double(3, 3, 3, 21));
                g2.draw(new Line2D.Double(3, 21, 21, 21));
                g2.fill(new RoundRectangle2D.Double(6, 13, 3.5, 6, 1, 1));
                g2.fill(new RoundRectangle2D.Double(11.25, 8, 3.5, 11, 1, 1));
                g2.fill(new RoundRectangle2D.Double(16.5, 11, 3.5, 8, 1, 1));
            }
        }
        g2.dispose();
    }

    
    public static class Engrenagem implements Icon {
        private final int tam;
        private final Color cor;

        public Engrenagem(int tam, Color cor) { this.tam = tam; this.cor = cor; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x + tam / 2.0, y + tam / 2.0);
            g2.setColor(cor);
            double dente = tam * 0.16;
            for (int i = 0; i < 8; i++) {
                g2.fill(new Rectangle2D.Double(-dente / 2, -tam / 2.0, dente, tam * 0.3));
                g2.rotate(Math.PI / 4);
            }
            double r = tam * 0.36;
            g2.fill(new Ellipse2D.Double(-r, -r, r * 2, r * 2));
            g2.setColor(Estilo.AZUL_ESCURO);
            double furo = tam * 0.14;
            g2.fill(new Ellipse2D.Double(-furo, -furo, furo * 2, furo * 2));
            g2.dispose();
        }

        @Override public int getIconWidth()  { return tam; }
        @Override public int getIconHeight() { return tam; }
    }
}
