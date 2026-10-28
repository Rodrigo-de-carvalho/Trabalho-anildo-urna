package br.com.urnacidada.persistencia;

import br.com.urnacidada.modelo.RegistroVoto;

import java.util.ArrayList;
import java.util.List;

/** Guarda os votos apenas na memória (útil para testes e demonstrações rápidas). */
public class MemoriaVotoRepository implements VotoRepository {

    private final List<RegistroVoto> votos = new ArrayList<>();

    @Override
    public synchronized void salvar(List<RegistroVoto> votosDoEleitor) {
        votos.addAll(votosDoEleitor);
    }

    @Override
    public synchronized List<RegistroVoto> listarTodos() {
        return List.copyOf(votos);
    }

    @Override
    public synchronized void zerar() {
        votos.clear();
    }

    @Override
    public String getDescricao() {
        return "Memória (os votos somem ao fechar)";
    }
}
