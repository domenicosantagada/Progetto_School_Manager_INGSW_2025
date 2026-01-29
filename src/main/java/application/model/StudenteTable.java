package application.model;

public class StudenteTable {
    private String nome;
    private String cognome;
    private String dataValutazione;
    private int voto;
    private String username;

    public StudenteTable(String nome, String cognome, String dataValutazione, int voto, String username) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataValutazione = dataValutazione;
        this.voto = voto;
        this.username = username;
    }

    public String nome() { return nome; }
    public String cognome() { return cognome; }
    public String dataValutazione() { return dataValutazione; }
    public int voto() { return voto; }
    public String username() { return username; }

    public void setVoto(int voto) { this.voto = voto; }
}