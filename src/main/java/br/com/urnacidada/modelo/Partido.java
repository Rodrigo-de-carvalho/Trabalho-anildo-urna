package br.com.urnacidada.modelo;

import java.util.Objects;

/**
 * Partido fictício participante da eleição simulada.
 * O número do partido (2 dígitos) é o prefixo do número de todos os seus candidatos.
 */
public final class Partido {

    private final int numero;
    private final String sigla;
    private final String nome;
    private final String corHex;

    public Partido(int numero, String sigla, String nome, String corHex) {
        if (numero < 10 || numero > 99) {
            throw new IllegalArgumentException("O número do partido deve ter 2 dígitos: " + numero);
        }
        this.numero = numero;
        this.sigla = Objects.requireNonNull(sigla, "sigla");
        this.nome = Objects.requireNonNull(nome, "nome");
        this.corHex = corHex == null ? "#607D8B" : corHex;
    }

    public int getNumero() {
        return numero;
    }

    public String getNumeroFormatado() {
        return String.valueOf(numero);
    }

    public String getSigla() {
        return sigla;
    }

    public String getNome() {
        return nome;
    }

    public String getCorHex() {
        return corHex;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Partido outro && outro.numero == numero;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }

    @Override
    public String toString() {
        return sigla + " (" + numero + ")";
    }
}
