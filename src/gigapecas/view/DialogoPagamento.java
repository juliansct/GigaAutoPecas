package gigapecas.view;

import gigapecas.model.FormaPagamento;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public final class DialogoPagamento {

    private DialogoPagamento() {}

    
    public static FormaPagamento mostrar(Component pai, double total) {
        JPanel painel = new JPanel(new BorderLayout(0, 14));

        
        JPanel caixaTotal = new JPanel(new BorderLayout());
        caixaTotal.setBackground(new Color(240, 253, 244));
        caixaTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel lblTxt = new JLabel("TOTAL A PAGAR");
        lblTxt.setFont(Estilo.fonte(Font.BOLD, 12));
        lblTxt.setForeground(Estilo.VERDE_HOVER);
        JLabel lblTotal = new JLabel(Estilo.MOEDA.format(total));
        lblTotal.setFont(Estilo.fonte(Font.BOLD, 24));
        lblTotal.setForeground(Estilo.VERDE_HOVER);
        caixaTotal.add(lblTxt, BorderLayout.WEST);
        caixaTotal.add(lblTotal, BorderLayout.EAST);
        painel.add(caixaTotal, BorderLayout.NORTH);

       
        JPanel formas = new JPanel(new GridLayout(2, 2, 8, 8));
        ButtonGroup grupo = new ButtonGroup();
        JToggleButton[] botoes = new JToggleButton[FormaPagamento.values().length];
        for (FormaPagamento f : FormaPagamento.values()) {
            JToggleButton b = new BotaoOpcao(f.getDescricao());
            b.setPreferredSize(new Dimension(170, 44));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            grupo.add(b);
            formas.add(b);
            botoes[f.ordinal()] = b;
        }

       
        JTextField txtRecebido = Estilo.estilizarCampo(new JTextField(10));
        JLabel lblTroco = new JLabel(" ");
        lblTroco.setFont(Estilo.fonte(Font.BOLD, 15));
        JPanel painelDinheiro = new JPanel(new BorderLayout(0, 6));
        painelDinheiro.add(Estilo.rotulo("VALOR RECEBIDO (R$)"), BorderLayout.NORTH);
        painelDinheiro.add(txtRecebido, BorderLayout.CENTER);
        painelDinheiro.add(lblTroco, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.add(formas, BorderLayout.NORTH);
        centro.add(painelDinheiro, BorderLayout.CENTER);
        painel.add(centro, BorderLayout.CENTER);

        Runnable atualizarTroco = () -> {
            Double recebido = lerValor(txtRecebido.getText());
            if (recebido == null) {
                lblTroco.setText(" ");
            } else if (recebido + 0.005 < total) {
                lblTroco.setForeground(Estilo.VERMELHO);
                lblTroco.setText("Faltam " + Estilo.MOEDA.format(total - recebido));
            } else {
                lblTroco.setForeground(Estilo.AZUL);
                lblTroco.setText("Troco: " + Estilo.MOEDA.format(recebido - total));
            }
        };
        txtRecebido.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { atualizarTroco.run(); }
            @Override public void removeUpdate(DocumentEvent e)  { atualizarTroco.run(); }
            @Override public void changedUpdate(DocumentEvent e) { atualizarTroco.run(); }
        });

       
        for (FormaPagamento f : FormaPagamento.values()) {
            botoes[f.ordinal()].addActionListener(e -> {
                painelDinheiro.setVisible(f == FormaPagamento.DINHEIRO);
                if (f == FormaPagamento.DINHEIRO) SwingUtilities.invokeLater(txtRecebido::requestFocusInWindow);
                SwingUtilities.getWindowAncestor(painel).pack();
            });
        }
        botoes[FormaPagamento.PIX.ordinal()].setSelected(true);
        painelDinheiro.setVisible(false);

        while (true) {
            int r = JOptionPane.showConfirmDialog(pai, painel, "Pagamento",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return null;

            FormaPagamento escolhida = null;
            for (FormaPagamento f : FormaPagamento.values()) {
                if (botoes[f.ordinal()].isSelected()) escolhida = f;
            }
            if (escolhida == FormaPagamento.DINHEIRO) {
                Double recebido = lerValor(txtRecebido.getText());
                if (recebido == null || recebido + 0.005 < total) {
                    JOptionPane.showMessageDialog(pai, "Informe um valor recebido igual ou maior que o total.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
            }
            return escolhida;
        }
    }

    
    private static Double lerValor(String texto) {
        try {
            String t = texto.trim().replace(".", "").replace(",", ".");
            if (texto.trim().matches("\\d+\\.\\d{1,2}")) t = texto.trim(); 
            return t.isEmpty() ? null : Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    
    private static class BotaoOpcao extends JToggleButton {
        BotaoOpcao(String texto) {
            super(texto);
            setFont(Estilo.fonte(Font.BOLD, 13));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            addChangeListener(e -> setForeground(isSelected() ? Color.WHITE : Estilo.TEXTO));
            setForeground(Estilo.TEXTO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean sel = isSelected();
            g2.setColor(sel ? Estilo.AZUL : getModel().isRollover() ? new Color(241, 245, 249) : Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(sel ? Estilo.AZUL : Estilo.BORDA);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
