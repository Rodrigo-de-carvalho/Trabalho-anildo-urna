package br.com.urnacidada.persistencia;

import br.com.urnacidada.modelo.RegistroVoto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Decorator que tenta gravar no repositório principal (nuvem) e, se falhar,
 * grava no de reserva (arquivo local). Assim nenhum voto se perde se a internet
 * da escola cair no meio da oficina. A apuração soma os dois.
 */
public class VotoRepositoryComReserva implements VotoRepository {

    private final VotoRepository principal;
    private final VotoRepository reserva;
    private final Consumer<String> avisos;

    public VotoRepositoryComReserva(VotoRepository principal, VotoRepository reserva, Consumer<String> avisos) {
        this.principal = principal;
        this.reserva = reserva;
        this.avisos = avisos;
    }

    @Override
    public void salvar(List<RegistroVoto> votosDoEleitor) throws PersistenciaException {
        try {
            principal.salvar(votosDoEleitor);
        } catch (PersistenciaException e) {
            reserva.salvar(votosDoEleitor);
            avisos.accept("Sem conexão com " + principal.getDescricao()
                    + ": voto guardado em " + reserva.getDescricao());
        }
    }

    @Override
    public List<RegistroVoto> listarTodos() throws PersistenciaException {
        List<RegistroVoto> todos = new ArrayList<>(reserva.listarTodos());
        try {
            todos.addAll(principal.listarTodos());
        } catch (PersistenciaException e) {
            avisos.accept("Não foi possível ler " + principal.getDescricao()
                    + ": a apuração mostra apenas os votos de " + reserva.getDescricao());
        }
        return todos;
    }

    @Override
    public void zerar() throws PersistenciaException {
        principal.zerar();
        reserva.zerar();
    }

    @Override
    public String getDescricao() {
        return principal.getDescricao() + " + reserva local";
    }
}
