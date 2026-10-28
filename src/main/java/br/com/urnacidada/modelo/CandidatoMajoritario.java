package br.com.urnacidada.modelo;

import java.util.List;
import java.util.Objects;

/** Candidato a cargo majoritário: concorre em chapa com um vice. */
public class CandidatoMajoritario extends Candidato {

    private final String nomeVice;

    public CandidatoMajoritario(String numero, String nomeUrna, String nomeVice,
                                Partido partido, CargoMajoritario cargo) {
        super(numero, nomeUrna, partido, cargo);
        this.nomeVice = Objects.requireNonNull(nomeVice, "nomeVice");
    }

    public String getNomeVice() {
        return nomeVice;
    }

    @Override
    public List<String> getInformacoesAdicionais() {
        return List.of("Vice-" + getCargo().getNome() + ": " + nomeVice);
    }
}
