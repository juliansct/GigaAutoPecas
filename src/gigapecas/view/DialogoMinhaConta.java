package gigapecas.view;

import gigapecas.dao.FuncionarioDAO;
import gigapecas.model.Funcionario;
import javax.swing.*;
import java.awt.*;


public final class DialogoMinhaConta {

   
    public enum Resultado { NADA, DADOS_ALTERADOS, LOGIN_ALTERADO }

    private DialogoMinhaConta() {}

    public static Resultado mostrar(Component pai, String usuario) {
        FuncionarioDAO dao = new FuncionarioDAO();
        Funcionario eu;
        try {
            eu = dao.buscarPorUsuario(usuario);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(pai, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return Resultado.NADA;
        }
        if (eu == null) return Resultado.NADA;

        JTextField txtNome = Estilo.estilizarCampo(new JTextField(eu.getNome(), 24));
        JTextField txtUsuario = Estilo.estilizarCampo(new JTextField(eu.getUsuario(), 24));
        JTextField txtCargo = Estilo.estilizarCampo(new JTextField(eu.getCargo(), 24));
        txtCargo.setEditable(false);
        txtCargo.setForeground(Estilo.TEXTO_SUAVE);
        txtCargo.setToolTipText("O cargo é alterado pelo gerente, na tela de Funcionários");
        JPasswordField txtSenhaAtual = Estilo.estilizarCampo(new JPasswordField(24));

        boolean[] trocouSenha = {false};
        JButton btnSenha = Estilo.botaoSecundario("Alterar minha senha");
        btnSenha.addActionListener(e -> {
            if (DialogoTrocarSenha.mostrar(SwingUtilities.getWindowAncestor(btnSenha), usuario, null, false)) {
                trocouSenha[0] = true;
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int y = 0;
        y = campo(form, g, y, "NOME", txtNome);
        y = campo(form, g, y, "USUÁRIO (LOGIN)", txtUsuario);
        y = campo(form, g, y, "CARGO (definido pelo gerente)", txtCargo);
        y = campo(form, g, y, "SENHA ATUAL (para salvar as alterações)", Estilo.comOlho(txtSenhaAtual));

        g.gridy = y;
        g.insets = new Insets(6, 0, 0, 0);
        JPanel linhaSenha = new JPanel(new BorderLayout());
        linhaSenha.add(new JSeparator(), BorderLayout.NORTH);
        JPanel caixaBotao = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        caixaBotao.add(btnSenha);
        linhaSenha.add(caixaBotao, BorderLayout.CENTER);
        form.add(linhaSenha, g);

        while (true) {
            int r = JOptionPane.showConfirmDialog(pai, form, "Minha conta",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return trocouSenha[0] ? Resultado.DADOS_ALTERADOS : Resultado.NADA;

            String nome = txtNome.getText().trim();
            String novoUsuario = txtUsuario.getText().trim().toLowerCase();
            boolean mudouAlgo = !nome.equals(eu.getNome()) || !novoUsuario.equals(eu.getUsuario());
            if (!mudouAlgo) return trocouSenha[0] ? Resultado.DADOS_ALTERADOS : Resultado.NADA;

            String erro = null;
            if (nome.isEmpty() || novoUsuario.isEmpty()) erro = "Nome e usuário não podem ficar vazios.";
            else if (novoUsuario.contains(" ")) erro = "O usuário não pode ter espaços.";
            else if (txtSenhaAtual.getPassword().length == 0) erro = "Digite sua senha atual para salvar.";
            if (erro != null) {
                JOptionPane.showMessageDialog(pai, erro, "Aviso", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            try {
                boolean ok = dao.atualizarPropriaConta(usuario, new String(txtSenhaAtual.getPassword()), nome, novoUsuario);
                if (!ok) {
                    JOptionPane.showMessageDialog(pai, "Senha atual incorreta.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    txtSenhaAtual.setText("");
                    continue;
                }
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(pai, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (!novoUsuario.equals(eu.getUsuario())) {
                JOptionPane.showMessageDialog(pai, "Dados atualizados!\nSeu login agora é \"" + novoUsuario
                        + "\". Entre novamente com ele.");
                return Resultado.LOGIN_ALTERADO;
            }
            JOptionPane.showMessageDialog(pai, "Dados atualizados!");
            return Resultado.DADOS_ALTERADOS;
        }
    }

    private static int campo(JPanel form, GridBagConstraints g, int y, String rotulo, JComponent campo) {
        g.gridy = y;
        g.insets = new Insets(0, 0, 4, 0);
        form.add(Estilo.rotulo(rotulo), g);
        g.gridy = y + 1;
        g.insets = new Insets(0, 0, 12, 0);
        form.add(campo, g);
        return y + 2;
    }
}
