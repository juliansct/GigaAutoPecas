package gigapecas.model;

import java.time.LocalDateTime;


public class Venda {
    private int id;
    private LocalDateTime dataHora;
    private String vendedor;
    private int quantidadeItens;
    private double total;
    private double custoTotal;
    private FormaPagamento formaPagamento;

    public Venda() {}

    public Venda(int id, LocalDateTime dataHora, String vendedor, int quantidadeItens, double total, double custoTotal) {
        this.id = id;
        this.dataHora = dataHora;
        this.vendedor = vendedor;
        this.quantidadeItens = quantidadeItens;
        this.total = total;
        this.custoTotal = custoTotal;
    }

    public double getLucro() { return total - custoTotal; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public int getQuantidadeItens() { return quantidadeItens; }
    public void setQuantidadeItens(int quantidadeItens) { this.quantidadeItens = quantidadeItens; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getCustoTotal() { return custoTotal; }
    public void setCustoTotal(double custoTotal) { this.custoTotal = custoTotal; }
}
