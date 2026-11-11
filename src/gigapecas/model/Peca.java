package gigapecas.model;

public class Peca {
    private int id;
    private String codigoBarras;
    private String nome;
    private String aplicacao;
    private double precoCusto;
    private double precoVenda;
    private int quantidadeEstoque;

    public Peca() {}

    public Peca(int id, String codigoBarras, String nome, String aplicacao, double precoCusto, double precoVenda, int quantidadeEstoque) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.aplicacao = aplicacao;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getAplicacao() { return aplicacao; }
    public void setAplicacao(String aplicacao) { this.aplicacao = aplicacao; }
    
    public double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }
    
    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }
    
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(int quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }
}