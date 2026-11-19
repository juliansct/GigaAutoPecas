package gigapecas.view;

import gigapecas.dao.FuncionarioDAO;
import javax.swing.*;
import java.awt.*;


public final class DialogoTrocarSenha {

    public static final int TAMANHO_MINIMO = 6;

    private DialogoTrocarSenha() {}

    
    public static boolean mostrar(Component pai, String usuario, String senhaAtualConhecida, boolean obrigatorio) {
        FuncionarioDAO dao = new FuncionarioDAO();

        while (true) {
            JPasswordField txtAtual = Estilo.estilizarCampo(new JPasswordField(22));
            JPasswordField txtNova = Estilo.estilizarCampo(new JPasswordField(22));
            JPasswordField txtConfirma = Estilo.estilizarCampo(new JPasswordField(22));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            int y = 0;

            if (obrigatorio) {
                JLabel aviso = new JLabel("<html><b>Primeiro acesso ou senha redefinida.</b><br>"
                        + "Crie uma senha só sua para continuar.</html>");
                aviso.setFont(Estilo.fonte(Font.PLAIN, 13));
                g.gridy = y++;
                g.insets = new Insets(0, 0, 14, 0);
                form.add(aviso, g);
            }
            if (senhaAtualConhecida == null) {
                y = campo(form, g, y, "SENHA ATUAL", Estilo.comOlho(txtAtual));
            }
            y = campo(form, g, y, "NOVA SENHA (mín. " + TAMANHO_MINIMO + " caracteres)", Estilo.comOlho(txtNova));
            campo(form, g, y, "CONFIRMAR NOVA SENHA", Estilo.comOlho(txtConfirma));

            int r = JOptionPane.showConfirmDialog(pai, form, obrigatorio ? "Criar sua senha" : "Alterar minha senha",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return false;

            String atual = senhaAtualConhecida != null ? senhaAtualConhecida : new String(txtAtual.getPassword());
            String nova = new String(txtNova.getPassword());
            String confirma = new String(txtConfirma.getPassword());

            String erro = null;
            if (nova.length() < TAMANHO_MINIMO) erro = "A nova senha precisa ter pelo menos " + TAMANHO_MINIMO + " caracteres.";
            else if (!nova.equals(confirma)) erro = "A confirmação não confere com a nova senha.";
            else if (nova.equals(atual)) erro = "A nova senha precisa ser diferente da atual.";
            if (erro != null) {
                JOptionPane.showMessageDialog(pai, erro, "Aviso", JOptionPane.WARNING_MESSAGE);
                continue; // mostra o formulário de novo
            }

            try {
                if (!dao.trocarPropriaSenha(usuario, atual, nova)) {
                    JOptionPane.showMessageDialog(pai, "Senha atual incorreta.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                JOptionPane.showMessageDialog(pai, "Senha alterada com sucesso!");
                return true;
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(pai, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }
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
