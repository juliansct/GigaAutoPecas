package gigapecas.view;

import gigapecas.dao.FornecedorDAO;
import gigapecas.model.Fornecedor;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class TelaFornecedores extends TelaListagem {

    private final FornecedorDAO dao = new FornecedorDAO();
    private final List<Fornecedor> linhas = new ArrayList<>();

    public TelaFornecedores() {
        super("Fornecedores", "De quem a loja compra (usado no cadastro da peça e na reposição)", "Novo Fornecedor",
                new String[]{"Nome Fantasia", "CNPJ", "Telefone", "E-mail"});
        tabela.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(260);

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
        for (Fornecedor f : dao.listar()) {
            linhas.add(f);
            modelo.addRow(new Object[]{f.getNomeFantasia(), traco(f.getCnpj()), traco(f.getTelefone()), traco(f.getEmail())});
        }
    }

    private static String traco(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    @Override
    protected void cadastrar() {
        formulario(null);
    }

    private void formulario(Fornecedor existente) {
        boolean novo = existente == null;
        JTextField txtNome = new JTextField(novo ? "" : existente.getNomeFantasia(), 26);
        JTextField txtCnpj = new JTextField(novo ? "" : existente.getCnpj(), 26);
        JTextField txtTelefone = new JTextField(novo ? "" : existente.getTelefone(), 26);
        JTextField txtEmail = new JTextField(novo ? "" : existente.getEmail(), 26);

        if (!mostrarFormulario(novo ? "Cadastrar Fornecedor" : "Editar Fornecedor",
                new String[]{"NOME FANTASIA *", "CNPJ", "TELEFONE", "E-MAIL"},
                new JComponent[]{txtNome, txtCnpj, txtTelefone, txtEmail})) return;

        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome fantasia.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Fornecedor f = novo ? new Fornecedor() : existente;
        f.setNomeFantasia(txtNome.getText().trim());
        f.setCnpj(txtCnpj.getText().trim());
        f.setTelefone(txtTelefone.getText().trim());
        f.setEmail(txtEmail.getText().trim());
        try {
            if (novo) dao.inserir(f); else dao.atualizar(f);
            recarregar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            recarregar();
        }
    }

    private void excluir() {
        int l = linhaSelecionada();
        if (l == -1) return;
        Fornecedor f = linhas.get(l);
        try {
            int qtd = dao.contarPecas(f.getId());
            String aviso = qtd == 0 ? "" : "\n" + qtd + " peça(s) deste fornecedor ficarão \"sem fornecedor\".";
            if (JOptionPane.showConfirmDialog(this, "Excluir o fornecedor [" + f.getNomeFantasia() + "]?" + aviso,
                    "Confirmação", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            dao.excluir(f.getId());
            recarregar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
