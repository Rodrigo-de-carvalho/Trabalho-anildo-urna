package br.com.urnacidada.modelo;

import java.util.Objects;

/**
 * Cargo em disputa. As subclasses definem as regras que mudam entre o sistema
 * proporcional e o majoritário (abstração + polimorfismo).
 */
public abstract class Cargo {

    private final String nome;
    private final int quantidadeDigitos;

    protected Cargo(String nome, int quantidadeDigitos) {
        if (quantidadeDigitos < 2) {
            throw new IllegalArgumentException("Um cargo precisa de pelo menos 2 dígitos");
        }
        this.nome = Objects.requireNonNull(nome, "nome");
        this.quantidadeDigitos = quantidadeDigitos;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidadeDigitos() {
        return quantidadeDigitos;
    }

    /** Se é possível votar apenas no partido (voto de legenda). */
    public abstract boolean permiteVotoLegenda();

    /** Nome do sistema eleitoral, exibido na apuração e no material educativo. */
    public abstract String getSistemaEleitoral();

    @Override
    public boolean equals(Object o) {
        return o instanceof Cargo outro && outro.nome.equalsIgnoreCase(nome);
    }

    @Override
    public int hashCode() {
        return nome.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return nome;
    }
}
