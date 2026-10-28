package br.com.urnacidada.persistencia;

/** Falha ao gravar ou ler votos. */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
