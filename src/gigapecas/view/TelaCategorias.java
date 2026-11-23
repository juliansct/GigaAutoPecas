package gigapecas.view;

import gigapecas.dao.CategoriaDAO;
import gigapecas.model.Categoria;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class TelaCategorias extends TelaListagem {

    private final CategoriaDAO dao = new CategoriaDAO();
    private final List<Categoria> linhas = new ArrayList<>();

    public TelaCategorias() {
        super("Categorias", "Classificação das peças (usada no cadastro e como filtro)", "Nova Categoria",
                new String[]{"Nome", "Descrição"});
        tabela.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(220);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(560);

        JButton btnExcluir = Estilo.botao("Excluir", Estilo.VERMELHO, Estilo.VERMELHO_HOVER);
        JButton btnEditar = Estilo.botaoSecundario("Editar");
        adicionarBotao(btnEditar);
        adicionarBotao(btnExcluir);
        btnEditar.addActionListener(e -> { int l = linhaSelecionada(); if (l != -1) formulario(linhas.get(l)); });
        btnExcluir.addActionListener(e -> excluir());
        aoDuploClique(l -> formulario(linhas.get(l)));
        recarregar();
    }

    @Override
    protected void carregar() {
        linhas.clear();
        for (Categoria c : dao.listar()) {
            linhas.add(c);
            modelo.addRow(new Object[]{c.getNome(), c.getDescricao()});
        }
    }

    @Override
    protected void cadastrar() {
        formulario(null);
    }

    private void formulario(Categoria existente) {
        boolean nova = existente == null;
        JTextField txtNome = new JTextField(nova ? "" : existente.getNome(), 26);
        JTextField txtDescricao = new JTextField(nova ? "" : existente.getDescricao(), 26);

        if (!mostrarFormulario(nova ? "Cadastrar Categoria" : "Editar Categoria",
                new String[]{"NOME *", "DESCRIÇÃO"},
                new JComponent[]{txtNome, txtDescricao})) return;

        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome da categoria.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Categoria c = nova ? new Categoria() : existente;
        c.setNome(txtNome.getText().trim());
        c.setDescricao(txtDescricao.getText().trim());
        try {
            if (nova) dao.inserir(c); else dao.atualizar(c);
            recarregar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            recarregar();
        }
    }

    private void excluir() {
        int l = linhaSelecionada();
        if (l == -1) return;
        Categoria c = linhas.get(l);
        try {
            int qtd = dao.contarPecas(c.getId());
            String aviso = qtd == 0 ? "" : "\n" + qtd + " peça(s) desta categoria ficarão \"sem categoria\".";
            if (JOptionPane.showConfirmDialog(this, "Excluir a categoria [" + c.getNome() + "]?" + aviso,
                    "Confirmação", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            dao.excluir(c.getId());
            recarregar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
