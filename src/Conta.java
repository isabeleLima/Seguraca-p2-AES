public class Conta {
    public String id;

    private double saldo;

    private Cliente cliente;

    private String cpf;

    private String telefone;

    private String endereco;

    private String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Conta(String id, double saldo, Cliente cliente, String cpf, String telefone, String endereco) {
        this.id = id;
        this.saldo = saldo;
        this.cliente = cliente;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}
