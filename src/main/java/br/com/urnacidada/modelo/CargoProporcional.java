package br.com.urnacidada.modelo;

/**
 * Cargo eleito pelo sistema proporcional (ex.: vereador, deputado).
 * Admite voto de legenda: o eleitor pode votar só no partido.
 */
public class CargoProporcional extends Cargo {

    public CargoProporcional(String nome, int quantidadeDigitos) {
        super(nome, quantidadeDigitos);
    }

    @Override
    public boolean permiteVotoLegenda() {
        return true;
    }

    @Override
    public String getSistemaEleitoral() {
        return "Proporcional";
    }
}
