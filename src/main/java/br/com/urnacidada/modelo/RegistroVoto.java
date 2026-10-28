package br.com.urnacidada.modelo;

import java.util.Objects;

/**
 * Registro anônimo de um voto, como fica salvo no banco de dados.
 * Propositalmente não guarda eleitor, horário nem ordem: assim como no Registro
 * Digital do Voto da urna real, não é possível associar um voto a uma pessoa.
 */
public record RegistroVoto(String cargo, TipoVoto tipo, String numero) {

    public RegistroVoto {
        Objects.requireNonNull(cargo, "cargo");
        Objects.requireNonNull(tipo, "tipo");
        numero = numero == null ? "" : numero;
    }
}
