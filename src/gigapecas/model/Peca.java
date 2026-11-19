package gigapecas.model;

public class Peca {
    private int id;
    private String codigoBarras;
    private String nome;
    private String aplicacao;
    private double precoCusto;
    private double precoVenda;
    private int quantidadeEstoque;
    private int estoqueMinimo = 5;

    
    private Integer categoriaId;
    private Integer fornecedorId;
 
    private String categoriaNome;
    private String fornecedorNome;

    public Peca() {}

    public Peca(int id, String codigoBarras, String nome, String aplicacao,
                double precoCusto, double precoVenda, int quantidadeEstoque) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.aplicacao = aplicacao;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public boolean isEstoqueBaixo() { return quantidadeEstoque <= estoqueMinimo; }

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

    public int getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }

    public Integer getFornecedorId() { return fornecedorId; }
    public void setFornecedorId(Integer fornecedorId) { this.fornecedorId = fornecedorId; }

    public String getCategoriaNome() { return categoriaNome; }
    public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }

    public String getFornecedorNome() { return fornecedorNome; }
    public void setFornecedorNome(String fornecedorNome) { this.fornecedorNome = fornecedorNome; }
}
