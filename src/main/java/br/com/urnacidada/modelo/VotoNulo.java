package br.com.urnacidada.modelo;

/** Voto nulo: número que não corresponde a nenhum candidato (nem partido, no proporcional). */
public class VotoNulo extends Voto {

    public VotoNulo(Cargo cargo) {
        super(cargo);
    }

    @Override
    public TipoVoto getTipo() {
        return TipoVoto.NULO;
    }

    @Override
    public String getNumeroRegistrado() {
        // O número digitado não é guardado: não identifica nada e só tornaria o voto rastreável.
        return "";
    }

    @Override
    public String getMensagemTela() {
        return "NÚMERO ERRADO - VOTO NULO";
    }
}
