package gigapecas.view;

import gigapecas.dao.PecaDAO;
import gigapecas.model.Peca;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaPrincipal extends JFrame {
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private PecaDAO pecaDAO;

    public TelaPrincipal() {
        pecaDAO = new PecaDAO();
        setTitle("GigaAutoPeças - Painel Principal");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

       
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuUsuario = new JMenu("Usuario");
        JMenuItem itemSair = new JMenuItem("Sair");
        
        itemSair.addActionListener(e -> System.exit(0));
        menuUsuario.add(itemSair);
		barraMenu.add(menuUsuario);
        setJMenuBar(barraMenu);

        
        JLabel lblTitulo = new JLabel(" Controle de Estoque - GigaAutoPeças", JLabel.LEFT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitulo, BorderLayout.NORTH);

        
        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Código", "Nome", "Aplicação", "Custo (R$)", "Venda (R$)", "Qtd"}, 0);
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

       
        JPanel painelBotoes = new JPanel();
        JButton btnNovo = new JButton("Nova Peça");
        JButton btnEntrada = new JButton("Registrar Entrada");
        JButton btnSaida = new JButton("Registrar Saída");
        JButton btnExcluir = new JButton("Excluir Peça");
        JButton btnAtualizar = new JButton("Atualizar Lista");

        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEntrada);
        painelBotoes.add(btnSaida);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnAtualizar);
        add(painelBotoes, BorderLayout.SOUTH);

       

       
        btnNovo.addActionListener(e -> {
            JTextField txtCodigo = new JTextField();
            JTextField txtNome = new JTextField();
            JTextField txtAplicacao = new JTextField();
            JTextField txtCusto = new JTextField();
            JTextField txtVenda = new JTextField();
            JTextField txtQtd = new JTextField();

            Object[] mensagem = {
                "Cód. Barras:", txtCodigo,
                "Nome da Peça:", txtNome,
                "Aplicação:", txtAplicacao,
                "Preço Custo (R$):", txtCusto,
                "Preço Venda (R$):", txtVenda,
                "Qtd. Estoque:", txtQtd
            };

            int opcao = JOptionPane.showConfirmDialog(this, mensagem, "Cadastrar Nova Peça", JOptionPane.OK_CANCEL_OPTION);
            if (opcao == JOptionPane.OK_OPTION) {
                try {
                    Peca p = new Peca();
                    p.setCodigoBarras(txtCodigo.getText());
                    p.setNome(txtNome.getText());
                    p.setAplicacao(txtAplicacao.getText());
                    p.setPrecoCusto(Double.parseDouble(txtCusto.getText().replace(",", ".")));
                    p.setPrecoVenda(Double.parseDouble(txtVenda.getText().replace(",", ".")));
                    p.setQuantidadeEstoque(Integer.parseInt(txtQtd.getText()));

                    pecaDAO.inserir(p);
                    carregarTabela();
                    JOptionPane.showMessageDialog(this, "Peça cadastrada com sucesso!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao cadastrar. Verifique se os campos numéricos estão corretos.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        
        btnAtualizar.addActionListener(e -> carregarTabela());

        
        btnEntrada.addActionListener(e -> {
            int linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada != -1) {
                int id = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
                String nomePeca = (String) modeloTabela.getValueAt(linhaSelecionada, 2);
                int qtdAtual = (int) modeloTabela.getValueAt(linhaSelecionada, 6);

                String qtdStr = JOptionPane.showInputDialog(this, "Quantidade de ENTRADA para [" + nomePeca + "]:");
                if (qtdStr != null && !qtdStr.isEmpty()) {
                    try {
                        int qtdAdicionar = Integer.parseInt(qtdStr);
                        if (qtdAdicionar <= 0) {
                            JOptionPane.showMessageDialog(this, "Digite um valor maior que zero!", "Aviso", JOptionPane.WARNING_MESSAGE);
                        } else {
                            int novaQtd = qtdAtual + qtdAdicionar;
                            pecaDAO.atualizarEstoque(id, novaQtd);
                            carregarTabela();
                            JOptionPane.showMessageDialog(this, "Entrada registrada com sucesso!");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Digite um número válido!", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma peça na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

      
        btnSaida.addActionListener(e -> {
            int linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada != -1) {
                int id = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
                String nomePeca = (String) modeloTabela.getValueAt(linhaSelecionada, 2);
                int qtdAtual = (int) modeloTabela.getValueAt(linhaSelecionada, 6);

                String qtdStr = JOptionPane.showInputDialog(this, "Quantidade de SAÍDA para [" + nomePeca + "]:");
                if (qtdStr != null && !qtdStr.isEmpty()) {
                    try {
                        int qtdBaixa = Integer.parseInt(qtdStr);
                        if (qtdBaixa <= 0) {
                            JOptionPane.showMessageDialog(this, "Digite um valor maior que zero!", "Aviso", JOptionPane.WARNING_MESSAGE);
                        } else if (qtdBaixa > qtdAtual) {
                            JOptionPane.showMessageDialog(this, "Estoque insuficiente!", "Aviso", JOptionPane.WARNING_MESSAGE);
                        } else {
                            int novaQtd = qtdAtual - qtdBaixa;
                            pecaDAO.atualizarEstoque(id, novaQtd);
                            carregarTabela();
                            JOptionPane.showMessageDialog(this, "Saída registrada com sucesso!");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Digite um número válido!", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma peça na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

   
        btnExcluir.addActionListener(e -> {
            int linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada != -1) {
                int id = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir esta peça?", "Confirmação", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    pecaDAO.excluir(id);
                    carregarTabela();
                    JOptionPane.showMessageDialog(this, "Peça excluída com sucesso!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma peça na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        carregarTabela();
    }

    public void carregarTabela() {
        modeloTabela.setRowCount(0);
        List<Peca> pecas = pecaDAO.listar();
        for (Peca p : pecas) {
            modeloTabela.addRow(new Object[]{
                p.getId(), p.getCodigoBarras(), p.getNome(), p.getAplicacao(),
                p.getPrecoCusto(), p.getPrecoVenda(), p.getQuantidadeEstoque()
            });
        }
    }
}