package gigapecas.view;

import gigapecas.dao.VendaDAO;
import gigapecas.model.FormaPagamento;
import gigapecas.model.ItemVenda;
import gigapecas.model.Venda;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TelaRelatorioVendas extends JFrame {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VendaDAO vendaDAO = new VendaDAO();

    private final CampoData campoInicio = new CampoData(LocalDate.now().withDayOfMonth(1));
    private final CampoData campoFim = new CampoData(LocalDate.now());
    /** Uma linha por forma de pagamento: "Pix ........ R$ 1.200,00 (12)". */
    private final JPanel painelFormas = new JPanel(new GridLayout(0, 1, 0, 6));

    private final JLabel valFaturamento = new JLabel();
    private final JLabel valQtdVendas = new JLabel();
    private final JLabel valTicket = new JLabel();
    private final JLabel valLucro = new JLabel();

    private DefaultTableModel modeloVendas, modeloTop;
    private JTable tabelaVendas;

    public TelaRelatorioVendas(String usuario) {
        setTitle("GigaAutoPeças - Relatório de Vendas");
        setSize(1320, 740);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        raiz.add(Estilo.cabecalho("Relatório de Vendas", usuario, Estilo.botaoVoltar(this)), BorderLayout.NORTH);

        JPanel conteudo = new JPanel(new BorderLayout(0, 16));
        conteudo.setOpaque(false);
        conteudo.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel topo = new JPanel(new BorderLayout(0, 16));
        topo.setOpaque(false);
        topo.add(criarFiltro(), BorderLayout.NORTH);
        JPanel resumo = new JPanel(new GridLayout(1, 4, 16, 0));
        resumo.setOpaque(false);
        resumo.add(Estilo.cartaoResumo("Faturamento", valFaturamento, Estilo.VERDE));
        resumo.add(Estilo.cartaoResumo("Nº de vendas", valQtdVendas, Estilo.AZUL));
        resumo.add(Estilo.cartaoResumo("Ticket médio", valTicket, Estilo.LARANJA));
        resumo.add(Estilo.cartaoResumo("Lucro bruto", valLucro, new Color(147, 51, 234)));
        topo.add(resumo, BorderLayout.CENTER);
        conteudo.add(topo, BorderLayout.NORTH);

        JPanel tabelas = new JPanel(new BorderLayout(16, 0));
        tabelas.setOpaque(false);
        tabelas.add(criarTabelaVendas(), BorderLayout.CENTER);
        tabelas.add(criarTabelaTop(), BorderLayout.EAST);
        conteudo.add(tabelas, BorderLayout.CENTER);

        raiz.add(conteudo, BorderLayout.CENTER);
        setContentPane(raiz);

        carregar(); 
    }

    private JPanel criarFiltro() {
        Estilo.Cartao cartao = new Estilo.Cartao(new FlowLayout(FlowLayout.LEFT, 10, 0), 14);
        cartao.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 16));

        JLabel lbl = new JLabel("Período:");
        lbl.setFont(Estilo.fonte(Font.BOLD, 13));
        cartao.add(lbl);
        cartao.add(campoInicio);
        JLabel ate = new JLabel("até");
        ate.setForeground(Estilo.TEXTO_SUAVE);
        cartao.add(ate);
        cartao.add(campoFim);
        JButton btnFiltrar = Estilo.botao("Filtrar", Estilo.AZUL, Estilo.AZUL_HOVER);
        cartao.add(btnFiltrar);
        cartao.add(Box.createHorizontalStrut(16));

        JButton btnHoje = Estilo.botaoSecundario("Hoje");
        JButton btn7 = Estilo.botaoSecundario("Últimos 7 dias");
        JButton btnMes = Estilo.botaoSecundario("Este mês");
        JButton btnAno = Estilo.botaoSecundario("Este ano");
        cartao.add(btnHoje);
        cartao.add(btn7);
        cartao.add(btnMes);
        cartao.add(btnAno);

        LocalDate hoje = LocalDate.now();
        btnHoje.addActionListener(e -> definirPeriodo(hoje, hoje));
        btn7.addActionListener(e -> definirPeriodo(hoje.minusDays(6), hoje));
        btnMes.addActionListener(e -> definirPeriodo(hoje.withDayOfMonth(1), hoje));
        btnAno.addActionListener(e -> definirPeriodo(hoje.withDayOfYear(1), hoje));
        btnFiltrar.addActionListener(e -> carregar());
        campoInicio.aoMudar(this::carregar);  
        campoFim.aoMudar(this::carregar);
        return cartao;
    }

    private JPanel criarTabelaVendas() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 10), 14);
        cartao.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(Estilo.titulo("Vendas do período", 16), BorderLayout.WEST);
        JLabel dica = new JLabel("Duplo clique para ver os itens");
        dica.setFont(Estilo.fonte(Font.PLAIN, 12));
        dica.setForeground(Estilo.TEXTO_SUAVE);
        topo.add(dica, BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);

        modeloVendas = new DefaultTableModel(new Object[]{"Nº", "Data / Hora", "Vendedor", "Pagamento", "Itens", "Total", "Lucro"}, 0) {
            private final Class<?>[] tipos = {Integer.class, String.class, String.class, String.class, Integer.class, Double.class, Double.class};
            @Override public Class<?> getColumnClass(int c) { return tipos[c]; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaVendas = new JTable(modeloVendas);
        Estilo.estilizarTabela(tabelaVendas);
        tabelaVendas.setDefaultRenderer(Double.class, Estilo.renderMoeda());
        tabelaVendas.setDefaultRenderer(Integer.class, Estilo.renderNumero());
        int[] larguras = {55, 135, 150, 140, 55, 105, 100};
        for (int i = 0; i < larguras.length; i++) {
            tabelaVendas.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
            if (i != 2) tabelaVendas.getColumnModel().getColumn(i).setMinWidth(larguras[i]);
        }
        cartao.add(Estilo.rolagem(tabelaVendas), BorderLayout.CENTER);

        tabelaVendas.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaVendas.getSelectedRow() != -1) mostrarItens();
            }
        });
        return cartao;
    }

    private JPanel criarTabelaTop() {
        Estilo.Cartao cartao = new Estilo.Cartao(new BorderLayout(0, 10), 14);
        cartao.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        cartao.setPreferredSize(new Dimension(380, 0));
        cartao.add(Estilo.titulo("Peças mais vendidas", 16), BorderLayout.NORTH);

        modeloTop = new DefaultTableModel(new Object[]{"Peça", "Qtd", "Valor"}, 0) {
            private final Class<?>[] tipos = {String.class, Integer.class, Double.class};
            @Override public Class<?> getColumnClass(int c) { return tipos[c]; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabelaTop = new JTable(modeloTop);
        Estilo.estilizarTabela(tabelaTop);
        tabelaTop.setDefaultRenderer(Double.class, Estilo.renderMoeda());
        tabelaTop.setDefaultRenderer(Integer.class, Estilo.renderNumero());
        tabelaTop.getColumnModel().getColumn(0).setPreferredWidth(190);
        tabelaTop.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabelaTop.getColumnModel().getColumn(2).setPreferredWidth(100);
        cartao.add(Estilo.rolagem(tabelaTop), BorderLayout.CENTER);

        JPanel formas = new JPanel(new BorderLayout(0, 8));
        formas.setOpaque(false);
        formas.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        formas.add(Estilo.titulo("Por forma de pagamento", 14), BorderLayout.NORTH);
        painelFormas.setOpaque(false);
        formas.add(painelFormas, BorderLayout.CENTER);
        cartao.add(formas, BorderLayout.SOUTH);
        return cartao;
    }

    
    private void mostrarFormas(Map<FormaPagamento, Double> total, Map<FormaPagamento, Integer> qtd, double faturamento) {
        painelFormas.removeAll();
        for (FormaPagamento f : FormaPagamento.values()) {
            double valor = total.getOrDefault(f, 0.0);
            int n = qtd.getOrDefault(f, 0);
            int pct = faturamento == 0 ? 0 : (int) Math.round(valor * 100 / faturamento);
            JPanel linha = new JPanel(new BorderLayout());
            linha.setOpaque(false);
            JLabel nome = new JLabel(f + "  (" + n + ")");
            nome.setFont(Estilo.fonte(Font.PLAIN, 13));
            nome.setForeground(Estilo.TEXTO);
            JLabel val = new JLabel(Estilo.MOEDA.format(valor) + "   " + pct + "%");
            val.setFont(Estilo.fonte(Font.BOLD, 13));
            val.setForeground(n == 0 ? Estilo.TEXTO_SUAVE : Estilo.TEXTO);
            linha.add(nome, BorderLayout.WEST);
            linha.add(val, BorderLayout.EAST);
            painelFormas.add(linha);
        }
        painelFormas.revalidate();
        painelFormas.repaint();
    }

    private void definirPeriodo(LocalDate inicio, LocalDate fim) {
        campoInicio.setData(inicio);
        campoFim.setData(fim);
        carregar();
    }

    private void carregar() {
        LocalDate inicio = campoInicio.getData(), fim = campoFim.getData();
        if (inicio == null || fim == null) {
            JOptionPane.showMessageDialog(this, "Use datas no formato dd/mm/aaaa.", "Data inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fim.isBefore(inicio)) {
            JOptionPane.showMessageDialog(this, "A data final é anterior à inicial.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloVendas.setRowCount(0);
        modeloTop.setRowCount(0);
        try {
            List<Venda> vendas = vendaDAO.listarPorPeriodo(inicio, fim);
            double faturamento = 0, lucro = 0;
            Map<FormaPagamento, Double> totalPorForma = new EnumMap<>(FormaPagamento.class);
            Map<FormaPagamento, Integer> qtdPorForma = new EnumMap<>(FormaPagamento.class);
            for (Venda v : vendas) {
                modeloVendas.addRow(new Object[]{v.getId(), v.getDataHora().format(DATA_HORA), v.getVendedor(),
                        v.getFormaPagamento().toString(), v.getQuantidadeItens(), v.getTotal(), v.getLucro()});
                faturamento += v.getTotal();
                totalPorForma.merge(v.getFormaPagamento(), v.getTotal(), Double::sum);
                qtdPorForma.merge(v.getFormaPagamento(), 1, Integer::sum);
                lucro += v.getLucro();
            }
            valFaturamento.setText(Estilo.MOEDA.format(faturamento));
            valQtdVendas.setText(String.valueOf(vendas.size()));
            valTicket.setText(Estilo.MOEDA.format(vendas.isEmpty() ? 0 : faturamento / vendas.size()));
            valLucro.setText(Estilo.MOEDA.format(lucro));
            mostrarFormas(totalPorForma, qtdPorForma, faturamento);

            for (ItemVenda i : vendaDAO.maisVendidas(inicio, fim, 10)) {
                // em maisVendidas, precoUnitario guarda o valor total vendido da peça
                modeloTop.addRow(new Object[]{i.getNomePeca(), i.getQuantidade(), i.getPrecoUnitario()});
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarItens() {
        int linha = tabelaVendas.convertRowIndexToModel(tabelaVendas.getSelectedRow());
        int vendaId = (int) modeloVendas.getValueAt(linha, 0);

        DefaultTableModel m = new DefaultTableModel(new Object[]{"Peça", "Qtd", "Unit.", "Subtotal"}, 0) {
            private final Class<?>[] tipos = {String.class, Integer.class, Double.class, Double.class};
            @Override public Class<?> getColumnClass(int c) { return tipos[c]; }
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try {
            for (ItemVenda i : vendaDAO.listarItens(vendaId)) {
                m.addRow(new Object[]{i.getNomePeca(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal()});
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTable t = new JTable(m);
        Estilo.estilizarTabela(t);
        t.setDefaultRenderer(Double.class, Estilo.renderMoeda());
        t.setDefaultRenderer(Integer.class, Estilo.renderNumero());
        JScrollPane sp = Estilo.rolagem(t);
        sp.setPreferredSize(new Dimension(520, Math.min(320, 40 + m.getRowCount() * 35)));

        JPanel p = new JPanel(new BorderLayout(0, 10));
        JLabel info = new JLabel(modeloVendas.getValueAt(linha, 1) + "  •  Vendedor: " + modeloVendas.getValueAt(linha, 2)
                + "  •  " + modeloVendas.getValueAt(linha, 3)
                + "  •  Total: " + Estilo.MOEDA.format((double) modeloVendas.getValueAt(linha, 5)));
        info.setFont(Estilo.fonte(Font.PLAIN, 13));
        p.add(info, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, p, "Venda nº " + vendaId, JOptionPane.PLAIN_MESSAGE);
    }
}
