package gigapecas.model;

import java.time.LocalDateTime;


public class MovimentacaoEstoque {
    private LocalDateTime dataHora;
    private String tipo;          // ENTRADA ou SAIDA
    private int quantidade;
    private String fornecedor;    // preenchido nas entradas por reposição
    private Integer vendaId;      // preenchido nas saídas por venda
    private String funcionario;

    public MovimentacaoEstoque(LocalDateTime dataHora, String tipo, int quantidade,
                               String fornecedor, Integer vendaId, String funcionario) {
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.fornecedor = fornecedor;
        this.vendaId = vendaId;
        this.funcionario = funcionario;
    }

    
    public String getOrigem() {
        if (vendaId != null) return "Venda nº " + vendaId;
        if (fornecedor != null) return fornecedor;
        return "ENTRADA".equals(tipo) ? "Entrada sem fornecedor" : "-";
    }

    public LocalDateTime getDataHora() { return dataHora; }
    public String getTipo() { return tipo; }
    public int getQuantidade() { return quantidade; }
    public String getFornecedor() { return fornecedor; }
    public Integer getVendaId() { return vendaId; }
    public String getFuncionario() { return funcionario; }
}
