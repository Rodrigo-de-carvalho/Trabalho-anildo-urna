package br.com.urnacidada.modelo;

import java.util.Objects;

/** Voto apenas no partido, permitido somente em cargos proporcionais. */
public class VotoLegenda extends Voto {

    private final Partido partido;
    private final boolean numeroCandidatoInexistente;

    /**
     * @param numeroCandidatoInexistente {@code true} quando o eleitor digitou o número
     *        completo de um candidato que não existe, mas o partido existe.
     */
    public VotoLegenda(Cargo cargo, Partido partido, boolean numeroCandidatoInexistente) {
        super(cargo);
        if (!cargo.permiteVotoLegenda()) {
            throw new IllegalArgumentException("O cargo " + cargo + " não admite voto de legenda");
        }
        this.partido = Objects.requireNonNull(partido, "partido");
        this.numeroCandidatoInexistente = numeroCandidatoInexistente;
    }

    public Partido getPartido() {
        return partido;
    }

    @Override
    public TipoVoto getTipo() {
        return TipoVoto.LEGENDA;
    }

    @Override
    public String getNumeroRegistrado() {
        return partido.getNumeroFormatado();
    }

    @Override
    public String getMensagemTela() {
        return numeroCandidatoInexistente ? "CANDIDATO INEXISTENTE - VOTO DE LEGENDA" : "VOTO DE LEGENDA";
    }
}
