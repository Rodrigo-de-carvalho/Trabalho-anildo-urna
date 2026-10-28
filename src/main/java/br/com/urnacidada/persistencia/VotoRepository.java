package br.com.urnacidada.persistencia;

import br.com.urnacidada.modelo.RegistroVoto;

import java.util.List;

/**
 * Contrato de armazenamento dos votos. A urna não sabe se os votos vão para a nuvem
 * (Firestore), para um arquivo local ou para a memória: depende só desta interface.
 */
public interface VotoRepository {

    /** Grava, de uma só vez, todos os votos de um eleitor. */
    void salvar(List<RegistroVoto> votosDoEleitor) throws PersistenciaException;

    List<RegistroVoto> listarTodos() throws PersistenciaException;

    /** Apaga todos os votos (preparar a urna para uma nova oficina). */
    void zerar() throws PersistenciaException;

    /** Descrição curta exibida na barra de status, ex.: "Firebase (Cloud Firestore)". */
    String getDescricao();
}
