package gigapecas.view;

import com.gigapecas.util.CriptografiaUtil;
import gigapecas.dao.FuncionarioDAO;
import gigapecas.model.Funcionario;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class TelaFuncionarios extends TelaListagem {

    private static final String[] CARGOS = {"Gerente de Estoque", "Gerente de Vendas", "Vendedor", "Estoquista", "Caixa"};

    private final FuncionarioDAO dao = new FuncionarioDAO();
    private final String usuarioLogado;
    private final List<Funcionario> linhas = new ArrayList<>();

    public TelaFuncionarios(String usuarioLogado) {
        super("Funcionários", "Pessoas com acesso ao sistema", "Novo Funcionário",
                new String[]{"Nome", "Usuário", "Cargo", "Senha"});
        this.usuarioLogado = usuarioLogado;

        tabela.getColumnModel().getColumn(0).setMaxWidth(Integer.MAX_VALUE);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(280);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(220);

        JButton btnExcluir = Estilo.botao("Excluir", Estilo.VERMELHO, Estilo.VERMELHO_HOVER);
        JButton btnSenha = Estilo.botaoSecundario("Redefinir senha");
        JButton btnEditar = Estilo.botaoSecundario("Editar");
        btnSenha.setToolTipText("Para quem esqueceu a senha: gera uma senha provisória");
        adicionarBotao(btnSenha);
        adicionarBotao(btnEditar);
        adicionarBotao(btnExcluir);
        btnEditar.addActionListener(e -> { int l = linhaSelecionada(); if (l != -1) editar(linhas.get(l)); });
        aoDuploClique(l -> editar(linhas.get(l)));
        btnExcluir.addActionListener(e -> excluir());
        btnSenha.addActionListener(e -> redefinirSenha());
        recarregar();
    }

    @Override
    protected void carregar() {
        linhas.clear();
        for (Funcionario f : dao.listar()) {
            linhas.add(f);
            modelo.addRow(new Object[]{f.getNome(), f.getUsuario(), f.getCargo(),
                    f.isPrecisaTrocarSenha() ? "Provisória (aguardando troca)" : "Definida pelo funcionário"});
        }
    }

    @Override
    protected void cadastrar() {
        JTextField txtNome = new JTextField(26);
        JTextField txtUsuario = new JTextField(26);
        JComboBox<String> cbCargo = new JComboBox<>(CARGOS);
        cbCargo.setEditable(true); // permite digitar outro cargo
        cbCargo.setSelectedItem("Vendedor");
        cbCargo.setFont(Estilo.fonte(Font.PLAIN, 14));
        cbCargo.setPreferredSize(new Dimension(10, 40));

        if (!mostrarFormulario("Cadastrar Funcionário",
                new String[]{"NOME COMPLETO *", "USUÁRIO (LOGIN) *", "CARGO *"},
                new JComponent[]{txtNome, txtUsuario, cbCargo})) return;

        String nome = txtNome.getText().trim();
        String usuario = txtUsuario.getText().trim().toLowerCase();
        Object cargoSel = cbCargo.getSelectedItem();
        String cargo = cargoSel == null ? "" : cargoSel.toString().trim();

        String erro = null;
        if (nome.isEmpty() || usuario.isEmpty() || cargo.isEmpty()) erro = "Preencha nome, usuário e cargo.";
        else if (usuario.contains(" ")) erro = "O usuário não pode ter espaços.";
        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (dao.usuarioExiste(usuario)) {
                JOptionPane.showMessageDialog(this, "Já existe um funcionário com o usuário \"" + usuario + "\".",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String provisoria = CriptografiaUtil.gerarSenhaProvisoria();
            dao.inserir(new Funcionario(0, nome, usuario, cargo), provisoria);
            recarregar();
            mostrarSenhaProvisoria(nome, usuario, provisoria);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void editar(Funcionario f) {
        if (f.getUsuario().equalsIgnoreCase(usuarioLogado)) {
            JOptionPane.showMessageDialog(this, "Para alterar seus próprios dados, use \"Minha conta\" na tela de início.\n"
                    + "Seu cargo só pode ser alterado por outro gerente.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextField txtNome = new JTextField(f.getNome(), 26);
        JComboBox<String> cbCargo = new JComboBox<>(CARGOS);
        cbCargo.setEditable(true);
        cbCargo.setSelectedItem(f.getCargo());
        cbCargo.setFont(Estilo.fonte(Font.PLAIN, 14));
        cbCargo.setPreferredSize(new Dimension(10, 40));

        if (!mostrarFormulario("Editar " + f.getUsuario(), new String[]{"NOME COMPLETO *", "CARGO *"},
                new JComponent[]{txtNome, cbCargo})) return;

        String nome = txtNome.getText().trim();
        Object sel = cbCargo.getSelectedItem();
        String cargo = sel == null ? "" : sel.toString().trim();
        if (nome.isEmpty() || cargo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha nome e cargo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (f.isGerente() && !Funcionario.cargoEhGerente(cargo) && contarGerentes() <= 1) {
            JOptionPane.showMessageDialog(this, "Este é o único gerente: o sistema precisa de pelo menos um.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            dao.atualizarPeloGerente(f.getId(), nome, cargo);
            recarregar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void redefinirSenha() {
        int linha = linhaSelecionada();
        if (linha == -1) return;
        Funcionario f = linhas.get(linha);

        if (f.getUsuario().equalsIgnoreCase(usuarioLogado)) {
            JOptionPane.showMessageDialog(this, "Para trocar a sua própria senha, use \"Minha conta\" na tela de início.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Gerar uma senha provisória para " + f.getNome() + "?\n"
                        + "A senha atual dele deixa de funcionar e ele vai criar uma nova no próximo login.",
                "Redefinir senha", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            String provisoria = CriptografiaUtil.gerarSenhaProvisoria();
            dao.definirSenhaProvisoria(f.getId(), provisoria);
            recarregar();
            mostrarSenhaProvisoria(f.getNome(), f.getUsuario(), provisoria);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void mostrarSenhaProvisoria(String nome, String usuario, String senha) {
        JTextField campoSenha = new JTextField(senha);
        campoSenha.setEditable(false);
        campoSenha.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        campoSenha.setHorizontalAlignment(SwingConstants.CENTER);
        campoSenha.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel p = new JPanel(new BorderLayout(0, 10));
        JLabel topo = new JLabel("<html>Repasse para <b>" + nome + "</b>:<br>Usuário: <b>" + usuario
                + "</b><br>Senha provisória:</html>");
        topo.setFont(Estilo.fonte(Font.PLAIN, 13));
        JLabel rodape = new JLabel("<html><i>No primeiro login ele vai criar a própria senha.<br>"
                + "Esta senha não será mostrada de novo.</i></html>");
        rodape.setFont(Estilo.fonte(Font.PLAIN, 12));
        p.add(topo, BorderLayout.NORTH);
        p.add(campoSenha, BorderLayout.CENTER);
        p.add(rodape, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, p, "Senha provisória", JOptionPane.INFORMATION_MESSAGE);
    }

    private void excluir() {
        int linha = linhaSelecionada();
        if (linha == -1) return;
        Funcionario f = linhas.get(linha);

        if (f.getUsuario().equalsIgnoreCase(usuarioLogado)) {
            JOptionPane.showMessageDialog(this, "Você não pode excluir o próprio usuário enquanto está logado.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (f.isGerente() && contarGerentes() <= 1) {
            JOptionPane.showMessageDialog(this, "Este é o único gerente. Cadastre outro gerente antes de excluí-lo,\n"
                    + "senão ninguém mais consegue gerenciar funcionários.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Excluir o funcionário [" + f.getNome() + "]?\n"
                        + "As vendas dele continuam no relatório.",
                "Confirmação", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            dao.excluir(f.getId());
            recarregar();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int contarGerentes() {
        int n = 0;
        for (Funcionario f : linhas) if (f.isGerente()) n++;
        return n;
    }
}
