package gigapecas.model;

public class ItemVenda {
    private int pecaId;
    private String nomePeca;
    private int quantidade;
    private double precoUnitario;
    private double custoUnitario;

    public ItemVenda() {}

    public ItemVenda(int pecaId, String nomePeca, int quantidade, double precoUnitario, double custoUnitario) {
        this.pecaId = pecaId;
        this.nomePeca = nomePeca;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.custoUnitario = custoUnitario;
    }

    public double getSubtotal() { return precoUnitario * quantidade; }

    public int getPecaId() { return pecaId; }
    public void setPecaId(int pecaId) { this.pecaId = pecaId; }

    public String getNomePeca() { return nomePeca; }
    public void setNomePeca(String nomePeca) { this.nomePeca = nomePeca; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }

    public double getCustoUnitario() { return custoUnitario; }
    public void setCustoUnitario(double custoUnitario) { this.custoUnitario = custoUnitario; }
}
