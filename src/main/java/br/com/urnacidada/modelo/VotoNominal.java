package br.com.urnacidada.modelo;

import java.util.Objects;

/** Voto em um candidato existente. */
public class VotoNominal extends Voto {

    private final Candidato candidato;

    public VotoNominal(Candidato candidato) {
        super(Objects.requireNonNull(candidato, "candidato").getCargo());
        this.candidato = candidato;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    @Override
    public TipoVoto getTipo() {
        return TipoVoto.NOMINAL;
    }

    @Override
    public String getNumeroRegistrado() {
        return candidato.getNumero();
    }

    @Override
    public String getMensagemTela() {
        return "";
    }
}
