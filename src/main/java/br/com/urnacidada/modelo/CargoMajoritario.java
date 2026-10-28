package br.com.urnacidada.modelo;

/**
 * Cargo eleito pelo sistema majoritário (ex.: prefeito, presidente).
 * Não existe voto de legenda: número sem candidato é voto nulo.
 */
public class CargoMajoritario extends Cargo {

    public CargoMajoritario(String nome) {
        super(nome, 2);
    }

    @Override
    public boolean permiteVotoLegenda() {
        return false;
    }

    @Override
    public String getSistemaEleitoral() {
        return "Majoritário";
    }
}
