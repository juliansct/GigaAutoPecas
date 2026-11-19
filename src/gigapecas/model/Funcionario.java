package gigapecas.model;

public class Funcionario {
    private int id;
    private String nome;
    private String usuario;
    private String cargo;
    
    private boolean precisaTrocarSenha;

    public Funcionario() {}

    public Funcionario(int id, String nome, String usuario, String cargo) {
        this.id = id;
        this.nome = nome;
        this.usuario = usuario;
        this.cargo = cargo;
    }

    
    public static boolean cargoEhGerente(String cargo) {
        return cargo != null && cargo.toLowerCase().contains("gerente");
    }

    public boolean isGerente() { return cargoEhGerente(cargo); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public boolean isPrecisaTrocarSenha() { return precisaTrocarSenha; }
    public void setPrecisaTrocarSenha(boolean precisaTrocarSenha) { this.precisaTrocarSenha = precisaTrocarSenha; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
}
