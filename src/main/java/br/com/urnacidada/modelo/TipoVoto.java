package br.com.urnacidada.modelo;

/** Classificação de um voto, usada na persistência e na apuração. */
public enum TipoVoto {
    NOMINAL("Nominal"),
    LEGENDA("Legenda"),
    BRANCO("Branco"),
    NULO("Nulo");

    private final String descricao;

    TipoVoto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Votos válidos são os dados a candidatos ou a partidos (brancos e nulos não contam). */
    public boolean isValido() {
        return this == NOMINAL || this == LEGENDA;
    }
}
