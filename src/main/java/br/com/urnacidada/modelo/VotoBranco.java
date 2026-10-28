package br.com.urnacidada.modelo;

/** Voto em branco: o eleitor não escolhe ninguém. */
public class VotoBranco extends Voto {

    public VotoBranco(Cargo cargo) {
        super(cargo);
    }

    @Override
    public TipoVoto getTipo() {
        return TipoVoto.BRANCO;
    }

    @Override
    public String getNumeroRegistrado() {
        return "";
    }

    @Override
    public String getMensagemTela() {
        return "VOTO EM BRANCO";
    }
}
