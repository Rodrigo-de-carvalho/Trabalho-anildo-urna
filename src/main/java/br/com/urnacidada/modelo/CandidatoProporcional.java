package br.com.urnacidada.modelo;

import java.util.List;

/** Candidato a cargo proporcional (vereador, deputado...). */
public class CandidatoProporcional extends Candidato {

    public CandidatoProporcional(String numero, String nomeUrna, Partido partido, CargoProporcional cargo) {
        super(numero, nomeUrna, partido, cargo);
    }

    @Override
    public List<String> getInformacoesAdicionais() {
        return List.of();
    }
}
