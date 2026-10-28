package br.com.urnacidada.modelo;

import java.util.List;
import java.util.Objects;

/**
 * Candidato fictício. Cada tipo de candidatura sabe quais informações extras
 * deve mostrar na tela da urna (polimorfismo em {@link #getInformacoesAdicionais()}).
 */
public abstract class Candidato {

    private final String numero;
    private final String nomeUrna;
    private final Partido partido;
    private final Cargo cargo;

    protected Candidato(String numero, String nomeUrna, Partido partido, Cargo cargo) {
        this.numero = Objects.requireNonNull(numero, "numero");
        this.nomeUrna = Objects.requireNonNull(nomeUrna, "nomeUrna");
        this.partido = Objects.requireNonNull(partido, "partido");
        this.cargo = Objects.requireNonNull(cargo, "cargo");
        validarNumero();
    }

    private void validarNumero() {
        if (!numero.matches("\\d+") || numero.length() != cargo.getQuantidadeDigitos()) {
            throw new IllegalArgumentException("Número " + numero + " inválido para o cargo " + cargo
                    + " (esperados " + cargo.getQuantidadeDigitos() + " dígitos)");
        }
        if (!numero.startsWith(partido.getNumeroFormatado())) {
            throw new IllegalArgumentException("O número " + numero + " deve começar com o número do partido "
                    + partido.getNumero());
        }
    }

    public String getNumero() {
        return numero;
    }

    public String getNomeUrna() {
        return nomeUrna;
    }

    public Partido getPartido() {
        return partido;
    }

    public Cargo getCargo() {
        return cargo;
    }

    /** Linhas extras exibidas abaixo do nome (ex.: o vice). */
    public abstract List<String> getInformacoesAdicionais();

    @Override
    public String toString() {
        return numero + " - " + nomeUrna + " (" + partido.getSigla() + ")";
    }
}
