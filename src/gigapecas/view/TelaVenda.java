package gigapecas.view;

import gigapecas.dao.PecaDAO;
import gigapecas.dao.VendaDAO;
import gigapecas.model.FormaPagamento;
import gigapecas.model.ItemVenda;
import gigapecas.model.Peca;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class TelaVenda extends JFrame {

    private static final int P_ID = 0, P_CODIGO = 1, P_NOME = 2, P_CATEGORIA = 3, P_APLICACAO = 4, P_PRECO = 5, P_ESTOQUE = 6;

    private final String usuario;
    private final PecaDAO pecaDAO = new PecaDAO();
    private final VendaDAO vendaDAO = new VendaDAO();


    private final Map<Integer, Peca> pecas = new LinkedHashMap<>();
    
    private final Map<Integer, ItemVenda> carrinho = new LinkedHashMap<>();

    private DefaultTableModel modeloPecas, modeloCarrinho;
    private JTable tabelaPecas, tabelaCarrinho;
    private TableRowSorter<DefaultTableModel> ordenador;
    private JTextField txtBusca;
    private JSpinner spnQtd;
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblQtdItens = new JLabel();
    private JButton btnFinalizar;

    public TelaVenda(String usuario) {
        this.usuario = usuario;
        setTitle("GigaAutoPeças - Nova Venda");
        setSize(1360, 720);
        setMinimumSize(new Dimension(1000, 580));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        raiz.add(Estilo.cabecalho("Nova Venda", usuario, Estilo.botaoVoltar(this)), BorderLayout.NORTH);

        JPanel conteudo = new JPanel(new BorderLayout(18, 0));
        conteudo.setOpaque(false);
        conteudo.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        conteudo.add(criarPainelPecas(), BorderLayout.CENTER);
        conteudo.add(criarPainelCarrinho(), BorderLayout.EAST);
        raiz.add(conteudo, BorderLayout.CENTER);
        setContentPane(raiz);

        carregarPecas();
        atualizarCarrinho();
        SwingUtilities.invokeLater(() -> txtBusca.requestFocusInWindow());
    }

   
    private JPanel criarPainelPecas() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 12), 14);
        cartao.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topo = new JPanel(new BorderLayout(0, 10));
        topo.setOpaque(false);
        topo.add(Estilo.titulo("1. Escolha as peças", 16), BorderLayout.NORTH);

        JPanel linhaBusca = new JPanel(new BorderLayout(10, 0));
        linhaBusca.setOpaque(false);
        txtBusca = Estilo.estilizarCampo(new JTextField());
        txtBusca.setToolTipText("Digite nome, categoria, aplicação ou código de barras. Enter adiciona a peça encontrada.");
        linhaBusca.add(txtBusca, BorderLayout.CENTER);

        JPanel qtdEAdd = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        qtdEAdd.setOpaque(false);
        JLabel lblQtd = new JLabel("Qtd:");
        lblQtd.setFont(Estilo.fonte(Font.BOLD, 13));
        qtdEAdd.add(lblQtd);
        spnQtd = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spnQtd.setFont(Estilo.fonte(Font.PLAIN, 14));
        spnQtd.setPreferredSize(new Dimension(70, 40));
        qtdEAdd.add(spnQtd);
        JButton btnAdicionar = Estilo.botao("Adicionar ao carrinho", Estilo.AZUL, Estilo.AZUL_HOVER);
        qtdEAdd.add(btnAdicionar);
        linhaBusca.add(qtdEAdd, BorderLayout.EAST);
        topo.add(linhaBusca, BorderLayout.CENTER);

        JLabel dica = new JLabel("Busque por nome, categoria ou aplicação, ou bipe o código de barras e aperte Enter. Duplo clique também adiciona.");
        dica.setFont(Estilo.fonte(Font.PLAIN, 12));
        dica.setForeground(Estilo.TEXTO_SUAVE);
        topo.add(dica, BorderLayout.SOUTH);
        cartao.add(topo, BorderLayout.NORTH);

        modeloPecas = new DefaultTableModel(new Object[]{"ID", "Código", "Peça", "Categoria", "Aplicação", "Preço", "Estoque"}, 0) {
            private final Class<?>[] tipos = {Integer.class, String.class, String.class, String.class, String.class, Double.class, Integer.class};
            @Override public Class<?> getColumnClass(int c) { return tipos[c]; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaPecas = new JTable(modeloPecas);
        ordenador = new TableRowSorter<>(modeloPecas);
        tabelaPecas.setRowSorter(ordenador);
        Estilo.estilizarTabela(tabelaPecas);
        tabelaPecas.setDefaultRenderer(Double.class, Estilo.renderMoeda());
        tabelaPecas.setDefaultRenderer(Integer.class, Estilo.renderNumero());
        tabelaPecas.removeColumn(tabelaPecas.getColumnModel().getColumn(P_ID));
        
        int[] larg = {125, 200, 100, 130, 95, 75};
        for (int i = 0; i < larg.length; i++) tabelaPecas.getColumnModel().getColumn(i).setPreferredWidth(larg[i]);
        tabelaPecas.getColumnModel().getColumn(0).setMinWidth(125);
        tabelaPecas.getColumnModel().getColumn(4).setMinWidth(95);
        tabelaPecas.getColumnModel().getColumn(5).setMinWidth(75);
        cartao.add(Estilo.rolagem(tabelaPecas), BorderLayout.CENTER);

        
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String t = txtBusca.getText().trim();
                ordenador.setRowFilter(t.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(t), P_CODIGO, P_NOME, P_CATEGORIA, P_APLICACAO));
                if (tabelaPecas.getRowCount() > 0) tabelaPecas.setRowSelectionInterval(0, 0);
            }
            @Override public void insertUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e)  { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        txtBusca.addActionListener(e -> adicionarPorBusca());
        btnAdicionar.addActionListener(e -> adicionarSelecionada((int) spnQtd.getValue()));
        tabelaPecas.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) adicionarSelecionada((int) spnQtd.getValue());
            }
        });
        return cartao;
    }

    
    private JPanel criarPainelCarrinho() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 12), 14);
        cartao.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        cartao.setPreferredSize(new Dimension(450, 0));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(Estilo.titulo("2. Carrinho", 16), BorderLayout.WEST);
        JButton btnRemover = Estilo.botaoSecundario("Remover item");
        topo.add(btnRemover, BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);

        modeloCarrinho = new DefaultTableModel(new Object[]{"Peça", "Qtd", "Unit.", "Subtotal"}, 0) {
            private final Class<?>[] tipos = {String.class, Integer.class, Double.class, Double.class};
            @Override public Class<?> getColumnClass(int c) { return tipos[c]; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaCarrinho = new JTable(modeloCarrinho);
        Estilo.estilizarTabela(tabelaCarrinho);
        tabelaCarrinho.setDefaultRenderer(Double.class, Estilo.renderMoeda());
        tabelaCarrinho.setDefaultRenderer(Integer.class, Estilo.renderNumero());
        tabelaCarrinho.getColumnModel().getColumn(0).setPreferredWidth(170);
        tabelaCarrinho.getColumnModel().getColumn(1).setMinWidth(55);
        tabelaCarrinho.getColumnModel().getColumn(1).setMaxWidth(55);
        tabelaCarrinho.getColumnModel().getColumn(2).setMinWidth(100);
        tabelaCarrinho.getColumnModel().getColumn(3).setMinWidth(110);
        cartao.add(Estilo.rolagem(tabelaCarrinho), BorderLayout.CENTER);

      
        JPanel rodape = new JPanel(new BorderLayout(0, 12));
        rodape.setOpaque(false);

        JPanel caixaTotal = new JPanel(new BorderLayout());
        caixaTotal.setBackground(new Color(240, 253, 244));
        caixaTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JPanel textosTotal = new JPanel(new GridLayout(2, 1));
        textosTotal.setOpaque(false);
        JLabel lblTxtTotal = new JLabel("TOTAL DA VENDA");
        lblTxtTotal.setFont(Estilo.fonte(Font.BOLD, 12));
        lblTxtTotal.setForeground(Estilo.VERDE_HOVER);
        lblQtdItens.setFont(Estilo.fonte(Font.PLAIN, 12));
        lblQtdItens.setForeground(Estilo.TEXTO_SUAVE);
        textosTotal.add(lblTxtTotal);
        textosTotal.add(lblQtdItens);
        caixaTotal.add(textosTotal, BorderLayout.WEST);
        lblTotal.setFont(Estilo.fonte(Font.BOLD, 26));
        lblTotal.setForeground(Estilo.VERDE_HOVER);
        caixaTotal.add(lblTotal, BorderLayout.EAST);
        rodape.add(caixaTotal, BorderLayout.NORTH);

        JPanel botoes = new JPanel(new GridLayout(1, 2, 10, 0));
        botoes.setOpaque(false);
        JButton btnCancelar = Estilo.botaoSecundario("Cancelar venda");
        btnFinalizar = Estilo.botao("Finalizar Venda", Estilo.VERDE, Estilo.VERDE_HOVER);
        btnFinalizar.setFont(Estilo.fonte(Font.BOLD, 15));
        btnFinalizar.setPreferredSize(new Dimension(0, 48));
        botoes.add(btnCancelar);
        botoes.add(btnFinalizar);
        rodape.add(botoes, BorderLayout.SOUTH);
        cartao.add(rodape, BorderLayout.SOUTH);

        btnRemover.addActionListener(e -> removerItem());
        btnCancelar.addActionListener(e -> {
            if (carrinho.isEmpty()) return;
            if (JOptionPane.showConfirmDialog(this, "Esvaziar o carrinho?", "Cancelar venda",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                carrinho.clear();
                atualizarCarrinho();
            }
        });
        btnFinalizar.addActionListener(e -> finalizarVenda());
        return cartao;
    }

    
    private void carregarPecas() {
        pecas.clear();
        modeloPecas.setRowCount(0);
        try {
            for (Peca p : pecaDAO.listar()) {
                pecas.put(p.getId(), p);
                modeloPecas.addRow(new Object[]{p.getId(), p.getCodigoBarras() == null ? "—" : p.getCodigoBarras(), p.getNome(),
                        p.getCategoriaNome() == null ? "—" : p.getCategoriaNome(), p.getAplicacao(),
                        p.getPrecoVenda(), p.getQuantidadeEstoque()});
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void adicionarPorBusca() {
        String texto = txtBusca.getText().trim();
        if (texto.isEmpty()) return;
        for (Peca p : pecas.values()) {
            if (texto.equals(p.getCodigoBarras())) {
                adicionar(p, (int) spnQtd.getValue());
                txtBusca.setText("");
                return;
            }
        }
        if (tabelaPecas.getRowCount() > 0) {
            tabelaPecas.setRowSelectionInterval(0, 0);
            if (adicionarSelecionada((int) spnQtd.getValue())) txtBusca.setText("");
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private boolean adicionarSelecionada(int qtd) {
        int r = tabelaPecas.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma peça na lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        int id = (int) modeloPecas.getValueAt(tabelaPecas.convertRowIndexToModel(r), P_ID);
        return adicionar(pecas.get(id), qtd);
    }

    private boolean adicionar(Peca p, int qtd) {
        ItemVenda item = carrinho.get(p.getId());
        int jaNoCarrinho = item == null ? 0 : item.getQuantidade();
        if (jaNoCarrinho + qtd > p.getQuantidadeEstoque()) {
            JOptionPane.showMessageDialog(this,
                    "Estoque insuficiente para \"" + p.getNome() + "\".\nDisponível: " + p.getQuantidadeEstoque()
                            + (jaNoCarrinho > 0 ? " (já tem " + jaNoCarrinho + " no carrinho)" : ""),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (item == null) {
            carrinho.put(p.getId(), new ItemVenda(p.getId(), p.getNome(), qtd, p.getPrecoVenda(), p.getPrecoCusto()));
        } else {
            item.setQuantidade(jaNoCarrinho + qtd);
        }
        spnQtd.setValue(1);
        atualizarCarrinho();
        txtBusca.requestFocusInWindow();
        return true;
    }

    private void removerItem() {
        int r = tabelaCarrinho.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item do carrinho.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = new ArrayList<>(carrinho.keySet()).get(r);
        carrinho.remove(id);
        atualizarCarrinho();
    }

    private void atualizarCarrinho() {
        modeloCarrinho.setRowCount(0);
        double total = 0;
        int unidades = 0;
        for (ItemVenda i : carrinho.values()) {
            modeloCarrinho.addRow(new Object[]{i.getNomePeca(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal()});
            total += i.getSubtotal();
            unidades += i.getQuantidade();
        }
        lblTotal.setText(Estilo.MOEDA.format(total));
        lblQtdItens.setText(unidades == 1 ? "1 unidade" : unidades + " unidades");
        btnFinalizar.setEnabled(!carrinho.isEmpty());
    }

    private void finalizarVenda() {
        if (carrinho.isEmpty()) return;
        List<ItemVenda> itens = new ArrayList<>(carrinho.values());
        double total = 0;
        for (ItemVenda i : itens) total += i.getSubtotal();

        FormaPagamento forma = DialogoPagamento.mostrar(this, total);
        if (forma == null) return; 

        try {
            int numero = vendaDAO.registrar(itens, usuario, forma);
            JOptionPane.showMessageDialog(this,
                    "Venda nº " + numero + " registrada!\nTotal: " + Estilo.MOEDA.format(total) + "  •  " + forma,
                    "Venda concluída", JOptionPane.INFORMATION_MESSAGE);
            carrinho.clear();
            atualizarCarrinho();
            carregarPecas();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            carregarPecas(); 
        }
    }
}
