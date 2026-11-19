package gigapecas.model;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    PIX("Pix"),
    DEBITO("Cartão de débito"),
    CREDITO("Cartão de crédito");

    private final String descricao;

    FormaPagamento(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }

    @Override
    public String toString() { return descricao; }
}
