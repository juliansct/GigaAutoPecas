package gigapecas.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public abstract class TelaListagem extends JFrame {

    protected final DefaultTableModel modelo;
    protected final JTable tabela;
    private final JPanel painelBotoes;

    protected TelaListagem(String titulo, String descricao, String textoBotaoNovo, String[] colunas) {
        setTitle("GigaAutoPeças - " + titulo);
        setSize(1000, 620);
        setMinimumSize(new Dimension(760, 480));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        raiz.add(Estilo.cabecalho(titulo, null, Estilo.botaoVoltar(this)), BorderLayout.NORTH);

        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 14), 14);
        cartao.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilo.fonte(Font.BOLD, 20));
        lblTitulo.setForeground(Estilo.TEXTO);
        JLabel lblDesc = new JLabel(descricao);
        lblDesc.setFont(Estilo.fonte(Font.PLAIN, 13));
        lblDesc.setForeground(Estilo.TEXTO_SUAVE);
        textos.add(lblTitulo);
        textos.add(lblDesc);
        barra.add(textos, BorderLayout.WEST);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        painelBotoes = botoes;
        botoes.setOpaque(false);
        JButton btnAtualizar = Estilo.botaoSecundario("Atualizar");
        JButton btnNovo = Estilo.botao(textoBotaoNovo, Estilo.AZUL, Estilo.AZUL_HOVER);
        botoes.add(btnAtualizar);
        botoes.add(btnNovo);
        barra.add(botoes, BorderLayout.EAST);
        cartao.add(barra, BorderLayout.NORTH);

        modelo = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modelo);
        tabela.setAutoCreateRowSorter(true);
        Estilo.estilizarTabela(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(70);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDA));
        scroll.getViewport().setBackground(Estilo.BRANCO);
        cartao.add(scroll, BorderLayout.CENTER);

        JPanel margem = new JPanel(new BorderLayout());
        margem.setOpaque(false);
        margem.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        margem.add(cartao);
        raiz.add(margem, BorderLayout.CENTER);
        setContentPane(raiz);

        btnAtualizar.addActionListener(e -> recarregar());
        btnNovo.addActionListener(e -> cadastrar());
    }

    
    protected void aoDuploClique(java.util.function.IntConsumer acao) {
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() != -1) {
                    acao.accept(tabela.convertRowIndexToModel(tabela.getSelectedRow()));
                }
            }
        });
    }

   
    protected void adicionarBotao(JButton botao) {
        painelBotoes.add(botao, 0);
    }

   
    protected int linhaSelecionada() {
        int r = tabela.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return tabela.convertRowIndexToModel(r);
    }

    
    protected void recarregar() {
        modelo.setRowCount(0);
        try {
            carregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar os dados: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    protected abstract void carregar();

        protected abstract void cadastrar();

   
    protected boolean mostrarFormulario(String titulo, String[] rotulos, JComponent[] campos) {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        for (int i = 0; i < campos.length; i++) {
            if (campos[i] instanceof JTextField) Estilo.estilizarCampo((JTextField) campos[i]);
            g.gridy = i * 2;
            g.insets = new Insets(0, 0, 4, 0);
            form.add(Estilo.rotulo(rotulos[i]), g);
            g.gridy = i * 2 + 1;
            g.insets = new Insets(0, 0, 12, 0);
            form.add(campos[i], g);
        }
        return JOptionPane.showConfirmDialog(this, form, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }
}
