package gigapecas.view;

import gigapecas.dao.CategoriaDAO;
import gigapecas.dao.FornecedorDAO;
import gigapecas.dao.PecaDAO;
import gigapecas.model.Categoria;
import gigapecas.model.Fornecedor;
import gigapecas.model.MovimentacaoEstoque;
import gigapecas.model.Peca;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class TelaPecas extends JFrame {

    private static final int COL_CODIGO = 0, COL_NOME = 1, COL_CATEGORIA = 2, COL_FORNECEDOR = 3,
            COL_APLICACAO = 4, COL_CUSTO = 5, COL_VENDA = 6, COL_QTD = 7;
    private static final String TODAS = "Todas as categorias";
    private static final String SEM = "—";

    private final String usuario;
    private final PecaDAO pecaDAO = new PecaDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final FornecedorDAO fornecedorDAO = new FornecedorDAO();

    /** Peça de cada linha do modelo da tabela (mesmo índice). */
    private final List<Peca> linhas = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();
    private List<Fornecedor> fornecedores = new ArrayList<>();

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> ordenador;
    private JTextField txtBusca;
    private JComboBox<String> cbFiltroCategoria;

    private final JLabel valTotalPecas = new JLabel("0");
    private final JLabel valItens = new JLabel("0");
    private final JLabel valCusto = new JLabel(Estilo.MOEDA.format(0));
    private final JLabel valBaixo = new JLabel("0");

    public TelaPecas(String usuario) {
        this.usuario = usuario;
        setTitle("GigaAutoPeças - Peças");
        setSize(1240, 700);
        setMinimumSize(new Dimension(1000, 560));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        raiz.add(Estilo.cabecalho("Peças", usuario, Estilo.botaoVoltar(this)), BorderLayout.NORTH);

        JPanel conteudo = new JPanel(new BorderLayout(0, 16));
        conteudo.setOpaque(false);
        conteudo.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel resumo = new JPanel(new GridLayout(1, 4, 16, 0));
        resumo.setOpaque(false);
        resumo.add(Estilo.cartaoResumo("Peças cadastradas", valTotalPecas, Estilo.AZUL));
        resumo.add(Estilo.cartaoResumo("Itens em estoque", valItens, Estilo.VERDE));
        resumo.add(Estilo.cartaoResumo("Valor em estoque (custo)", valCusto, Estilo.LARANJA));
        resumo.add(Estilo.cartaoResumo("Abaixo do estoque mínimo", valBaixo, Estilo.VERMELHO));
        conteudo.add(resumo, BorderLayout.NORTH);
        conteudo.add(criarAreaTabela(), BorderLayout.CENTER);
        raiz.add(conteudo, BorderLayout.CENTER);

        setContentPane(raiz);
        carregarTudo();
    }

    private JPanel criarAreaTabela() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 12), 14);
        cartao.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        txtBusca = Estilo.estilizarCampo(new JTextField(16));
        txtBusca.setToolTipText("Buscar por código, nome, fornecedor ou aplicação");
        cbFiltroCategoria = new JComboBox<>();
        cbFiltroCategoria.setFont(Estilo.fonte(Font.PLAIN, 13));
        cbFiltroCategoria.setPreferredSize(new Dimension(180, 40));
        JLabel lblBusca = new JLabel("Buscar:");
        lblBusca.setFont(Estilo.fonte(Font.BOLD, 13));
        filtros.add(lblBusca);
        filtros.add(txtBusca);
        filtros.add(cbFiltroCategoria);
        barra.add(filtros, BorderLayout.WEST);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        JButton btnHistorico = Estilo.botaoSecundario("Histórico");
        JButton btnExcluir   = Estilo.botao("Excluir", Estilo.VERMELHO, Estilo.VERMELHO_HOVER);
        JButton btnEditar    = Estilo.botaoSecundario("Editar");
        JButton btnRepor     = Estilo.botao("Repor Estoque", Estilo.VERDE, Estilo.VERDE_HOVER);
        JButton btnNovo      = Estilo.botao("Nova Peça", Estilo.AZUL, Estilo.AZUL_HOVER);
        btnRepor.setToolTipText("Registrar chegada de mercadoria do fornecedor");
        btnHistorico.setToolTipText("Entradas e saídas da peça selecionada");
        botoes.add(btnHistorico);
        botoes.add(btnExcluir);
        botoes.add(btnEditar);
        botoes.add(btnRepor);
        botoes.add(btnNovo);
        barra.add(botoes, BorderLayout.EAST);

        cartao.add(barra, BorderLayout.NORTH);
        cartao.add(criarTabela(), BorderLayout.CENTER);

        JLabel dica = new JLabel("Duplo clique para editar. Linhas em vermelho estão no estoque mínimo ou abaixo dele.");
        dica.setFont(Estilo.fonte(Font.PLAIN, 12));
        dica.setForeground(Estilo.TEXTO_SUAVE);
        cartao.add(dica, BorderLayout.SOUTH);

        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { aplicarFiltro(); }
            @Override public void removeUpdate(DocumentEvent e)  { aplicarFiltro(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        cbFiltroCategoria.addActionListener(e -> aplicarFiltro());

        btnNovo.addActionListener(e -> abrirFormulario(null));
        btnEditar.addActionListener(e -> { Peca p = selecionada(); if (p != null) abrirFormulario(p); });
        btnRepor.addActionListener(e -> { Peca p = selecionada(); if (p != null) reporEstoque(p); });
        btnExcluir.addActionListener(e -> { Peca p = selecionada(); if (p != null) excluir(p); });
        btnHistorico.addActionListener(e -> { Peca p = selecionada(); if (p != null) mostrarHistorico(p); });
        tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() != -1) {
                    abrirFormulario(linhas.get(tabela.convertRowIndexToModel(tabela.getSelectedRow())));
                }
            }
        });
        return cartao;
    }

    private JScrollPane criarTabela() {
        modeloTabela = new DefaultTableModel(new Object[]{"Código", "Nome", "Categoria", "Fornecedor",
                "Aplicação", "Custo", "Venda", "Qtd"}, 0) {
            private final Class<?>[] tipos = {String.class, String.class, String.class, String.class,
                    String.class, Double.class, Double.class, Integer.class};
            @Override public Class<?> getColumnClass(int c) { return tipos[c]; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabela = new JTable(modeloTabela);
        ordenador = new TableRowSorter<>(modeloTabela);
        tabela.setRowSorter(ordenador);
        Estilo.estilizarTabela(tabela);

        RenderizadorCelula r = new RenderizadorCelula();
        tabela.setDefaultRenderer(Object.class, r);
        tabela.setDefaultRenderer(String.class, r);
        tabela.setDefaultRenderer(Integer.class, r);
        tabela.setDefaultRenderer(Double.class, r);

        int[] larguras = {125, 210, 110, 160, 160, 95, 95, 60};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.getColumnModel().getColumn(COL_QTD).setMaxWidth(80);
        return Estilo.rolagem(tabela);
    }

   
    private class RenderizadorCelula extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            l.setHorizontalAlignment(c >= COL_CUSTO ? SwingConstants.RIGHT : SwingConstants.LEFT);
            l.setFont(Estilo.fonte(c == COL_NOME || c == COL_QTD ? Font.BOLD : Font.PLAIN, 13));
            if (v instanceof Double) l.setText(Estilo.MOEDA.format((Double) v));

            Peca p = linhas.get(t.convertRowIndexToModel(r));
            boolean baixo = p.isEstoqueBaixo();
            if (sel)        l.setBackground(Estilo.SELECAO);
            else if (baixo) l.setBackground(Estilo.ALERTA_FUNDO);
            else            l.setBackground(r % 2 == 0 ? Estilo.BRANCO : Estilo.LINHA_ALTERNADA);
            l.setForeground(baixo && c == COL_QTD ? Estilo.VERMELHO
                    : SEM.equals(v) ? Estilo.TEXTO_SUAVE : Estilo.TEXTO);
            if (c == COL_QTD) l.setToolTipText("Mínimo: " + p.getEstoqueMinimo());
            return l;
        }
    }

    
    private void carregarTudo() {
        try {
            categorias = categoriaDAO.listar();
            fornecedores = fornecedorDAO.listar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        Object filtroAtual = cbFiltroCategoria.getSelectedItem();
        cbFiltroCategoria.removeAllItems();
        cbFiltroCategoria.addItem(TODAS);
        for (Categoria c : categorias) cbFiltroCategoria.addItem(c.getNome());
        cbFiltroCategoria.addItem("Sem categoria");
        if (filtroAtual != null) cbFiltroCategoria.setSelectedItem(filtroAtual);
        carregarTabela();
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        linhas.clear();
        List<Peca> pecas;
        try {
            pecas = pecaDAO.listar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int itens = 0, baixo = 0;
        double valor = 0;
        for (Peca p : pecas) {
            linhas.add(p);
            modeloTabela.addRow(new Object[]{
                p.getCodigoBarras() == null ? SEM : p.getCodigoBarras(),
                p.getNome(),
                p.getCategoriaNome() == null ? SEM : p.getCategoriaNome(),
                p.getFornecedorNome() == null ? SEM : p.getFornecedorNome(),
                p.getAplicacao() == null || p.getAplicacao().isBlank() ? SEM : p.getAplicacao(),
                p.getPrecoCusto(), p.getPrecoVenda(), p.getQuantidadeEstoque()
            });
            itens += p.getQuantidadeEstoque();
            valor += p.getPrecoCusto() * p.getQuantidadeEstoque();
            if (p.isEstoqueBaixo()) baixo++;
        }
        valTotalPecas.setText(String.valueOf(pecas.size()));
        valItens.setText(String.valueOf(itens));
        valCusto.setText(Estilo.MOEDA.format(valor));
        valBaixo.setText(String.valueOf(baixo));
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (ordenador == null) return;
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();
        String t = txtBusca.getText().trim();
        if (!t.isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(t), COL_CODIGO, COL_NOME, COL_FORNECEDOR, COL_APLICACAO));
        }
        Object cat = cbFiltroCategoria.getSelectedItem();
        if (cat != null && !TODAS.equals(cat)) {
            String alvo = "Sem categoria".equals(cat) ? SEM : cat.toString();
            filtros.add(RowFilter.regexFilter("^" + Pattern.quote(alvo) + "$", COL_CATEGORIA));
        }
        ordenador.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
    }

    private Peca selecionada() {
        int r = tabela.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma peça na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return linhas.get(tabela.convertRowIndexToModel(r));
    }

    private static final String NENHUM = "— Nenhum —";

    private JComboBox<Object> combo(List<?> itens, Integer idSelecionado) {
        JComboBox<Object> cb = new JComboBox<>();
        cb.addItem(NENHUM);
        for (Object o : itens) {
            cb.addItem(o);
            int id = o instanceof Categoria ? ((Categoria) o).getId() : ((Fornecedor) o).getId();
            if (idSelecionado != null && id == idSelecionado) cb.setSelectedItem(o);
        }
        cb.setFont(Estilo.fonte(Font.PLAIN, 14));
        cb.setPreferredSize(new Dimension(10, 40));
        return cb;
    }

    private static Integer idDoCombo(JComboBox<Object> cb) {
        Object o = cb.getSelectedItem();
        if (o instanceof Categoria) return ((Categoria) o).getId();
        if (o instanceof Fornecedor) return ((Fornecedor) o).getId();
        return null;
    }



    private void abrirFormulario(Peca peca) {
        boolean novo = peca == null;
        JTextField txtCodigo = Estilo.estilizarCampo(new JTextField(24));
        JTextField txtNome = Estilo.estilizarCampo(new JTextField(24));
        JTextField txtAplicacao = Estilo.estilizarCampo(new JTextField(24));
        JComboBox<Object> cbCategoria = combo(categorias, novo ? null : peca.getCategoriaId());
        JComboBox<Object> cbFornecedor = combo(fornecedores, novo ? null : peca.getFornecedorId());
        JTextField txtCusto = Estilo.estilizarCampo(new JTextField(8));
        JTextField txtVenda = Estilo.estilizarCampo(new JTextField(8));
        JTextField txtQtd = Estilo.estilizarCampo(new JTextField("0", 6));
        JTextField txtMinimo = Estilo.estilizarCampo(new JTextField("5", 6));

        if (!novo) {
            txtCodigo.setText(peca.getCodigoBarras());
            txtNome.setText(peca.getNome());
            txtAplicacao.setText(peca.getAplicacao());
            txtCusto.setText(String.format("%.2f", peca.getPrecoCusto()));
            txtVenda.setText(String.format("%.2f", peca.getPrecoVenda()));
            txtQtd.setText(String.valueOf(peca.getQuantidadeEstoque()));
            txtQtd.setEnabled(false);
            txtQtd.setToolTipText("A quantidade muda por \"Repor Estoque\" ou por venda");
            txtMinimo.setText(String.valueOf(peca.getEstoqueMinimo()));
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridwidth = 4;
        int y = 0;
        y = adicionarCampo(form, g, y, 0, "NOME DA PEÇA *", txtNome);
        y = adicionarCampo(form, g, y, 0, "CÓDIGO DE BARRAS", txtCodigo);
        y = adicionarCampo(form, g, y, 0, "APLICAÇÃO (VEÍCULOS)", txtAplicacao);
        g.gridwidth = 2;
        adicionarCampo(form, g, y, 0, "CATEGORIA", cbCategoria);
        g.gridx = 2;
        y = adicionarCampo(form, g, y, 2, "FORNECEDOR", cbFornecedor);
        g.gridwidth = 1;
        adicionarCampo(form, g, y, 0, "CUSTO (R$) *", txtCusto);
        adicionarCampo(form, g, y, 1, "VENDA (R$) *", txtVenda);
        adicionarCampo(form, g, y, 2, novo ? "QTD. INICIAL" : "EM ESTOQUE", txtQtd);
        adicionarCampo(form, g, y, 3, "ESTOQUE MÍN.", txtMinimo);

        int opcao = JOptionPane.showConfirmDialog(this, form, novo ? "Cadastrar Nova Peça" : "Editar Peça",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) return;

        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome da peça.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Peca p = novo ? new Peca() : peca;
        try {
            p.setCodigoBarras(txtCodigo.getText().trim());
            p.setNome(txtNome.getText().trim());
            p.setAplicacao(txtAplicacao.getText().trim());
            p.setCategoriaId(idDoCombo(cbCategoria));
            p.setFornecedorId(idDoCombo(cbFornecedor));
            p.setPrecoCusto(Double.parseDouble(txtCusto.getText().trim().replace(",", ".")));
            p.setPrecoVenda(Double.parseDouble(txtVenda.getText().trim().replace(",", ".")));
            p.setEstoqueMinimo(Integer.parseInt(txtMinimo.getText().trim()));
            if (novo) p.setQuantidadeEstoque(Integer.parseInt(txtQtd.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique os campos numéricos (preços, quantidade e mínimo).",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (p.getPrecoCusto() < 0 || p.getPrecoVenda() < 0 || p.getQuantidadeEstoque() < 0 || p.getEstoqueMinimo() < 0) {
            JOptionPane.showMessageDialog(this, "Valores não podem ser negativos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (p.getPrecoVenda() < p.getPrecoCusto()
                && JOptionPane.showConfirmDialog(this, "O preço de venda está abaixo do custo. Salvar mesmo assim?",
                "Atenção", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (novo) pecaDAO.inserir(p, usuario); else pecaDAO.atualizar(p);
            carregarTabela();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int adicionarCampo(JPanel form, GridBagConstraints g, int y, int x, String rotulo, JComponent campo) {
        g.gridx = x;
        g.gridy = y;
        g.insets = new Insets(0, x == 0 ? 0 : 10, 4, 0);
        form.add(Estilo.rotulo(rotulo), g);
        g.gridy = y + 1;
        g.insets = new Insets(0, x == 0 ? 0 : 10, 12, 0);
        form.add(campo, g);
        return y + 2;
    }


    private void reporEstoque(Peca p) {
        JTextField txtQtd = Estilo.estilizarCampo(new JTextField(8));
        JComboBox<Object> cbFornecedor = combo(fornecedores, p.getFornecedorId());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        JLabel info = new JLabel("<html><b>" + p.getNome() + "</b><br>Estoque atual: "
                + p.getQuantidadeEstoque() + "</html>");
        info.setFont(Estilo.fonte(Font.PLAIN, 13));
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(0, 0, 14, 0);
        form.add(info, g);
        int y = adicionarCampo(form, g, 1, 0, "QUANTIDADE QUE CHEGOU", txtQtd);
        adicionarCampo(form, g, y, 0, "FORNECEDOR", cbFornecedor);
        cbFornecedor.setPreferredSize(new Dimension(300, 40));
        SwingUtilities.invokeLater(txtQtd::requestFocusInWindow);

        if (JOptionPane.showConfirmDialog(this, form, "Repor Estoque", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            int qtd = Integer.parseInt(txtQtd.getText().trim());
            if (qtd <= 0) {
                JOptionPane.showMessageDialog(this, "Digite um valor maior que zero!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            pecaDAO.reporEstoque(p.getId(), qtd, idDoCombo(cbFornecedor), usuario);
            carregarTabela();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite um número válido!", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir(Peca p) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Excluir a peça [" + p.getNome() + "]?\nO histórico de estoque dela também será apagado.\n"
                        + "As vendas antigas continuam no relatório.",
                "Confirmação", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            pecaDAO.excluir(p.getId());
            carregarTabela();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarHistorico(Peca p) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DefaultTableModel m = new DefaultTableModel(new Object[]{"Data / Hora", "Tipo", "Qtd", "Origem / Destino", "Funcionário"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try {
            for (MovimentacaoEstoque mov : pecaDAO.listarMovimentacoes(p.getId())) {
                boolean entrada = "ENTRADA".equals(mov.getTipo());
                m.addRow(new Object[]{mov.getDataHora().format(fmt), entrada ? "Entrada" : "Saída",
                        (entrada ? "+" : "−") + mov.getQuantidade(), mov.getOrigem(),
                        mov.getFuncionario() == null ? SEM : mov.getFuncionario()});
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTable t = new JTable(m);
        Estilo.estilizarTabela(t);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, false, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!s) l.setBackground(r % 2 == 0 ? Estilo.BRANCO : Estilo.LINHA_ALTERNADA);
                boolean entrada = "Entrada".equals(tb.getValueAt(r, 1));
                l.setForeground(c == 1 || c == 2 ? (entrada ? Estilo.VERDE_HOVER : Estilo.VERMELHO) : Estilo.TEXTO);
                l.setHorizontalAlignment(c == 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return l;
            }
        });
        int[] larg = {130, 70, 50, 200, 150};
        for (int i = 0; i < larg.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(larg[i]);
        JScrollPane sp = Estilo.rolagem(t);
        sp.setPreferredSize(new Dimension(640, 320));

        JPanel painel = new JPanel(new BorderLayout(0, 10));
        JLabel info = new JLabel(p.getNome() + "  •  Estoque atual: " + p.getQuantidadeEstoque()
                + "  •  Mínimo: " + p.getEstoqueMinimo());
        info.setFont(Estilo.fonte(Font.BOLD, 13));
        painel.add(info, BorderLayout.NORTH);
        painel.add(m.getRowCount() == 0 ? new JLabel("Nenhuma movimentação registrada.") : sp, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, painel, "Histórico de Estoque", JOptionPane.PLAIN_MESSAGE);
    }
}
