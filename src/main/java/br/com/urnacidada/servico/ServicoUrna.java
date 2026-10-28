package br.com.urnacidada.servico;

import br.com.urnacidada.modelo.Eleicao;
import br.com.urnacidada.modelo.RegistroVoto;
import br.com.urnacidada.modelo.Voto;
import br.com.urnacidada.persistencia.PersistenciaException;
import br.com.urnacidada.persistencia.VotoRepository;

import java.util.List;
import java.util.Objects;

/** Fachada usada pela interface: abre sessões de votação, grava votos e apura. */
public class ServicoUrna {

    private final Eleicao eleicao;
    private final VotoRepository repositorio;

    public ServicoUrna(Eleicao eleicao, VotoRepository repositorio) {
        this.eleicao = Objects.requireNonNull(eleicao, "eleicao");
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio");
    }

    public Eleicao getEleicao() {
        return eleicao;
    }

    public VotoRepository getRepositorio() {
        return repositorio;
    }

    public SessaoVotacao novaSessao() {
        return new SessaoVotacao(eleicao);
    }

    /** Grava os votos de um eleitor que concluiu todos os cargos. */
    public void registrar(SessaoVotacao sessao) throws PersistenciaException {
        if (!sessao.isFinalizada()) {
            throw new IllegalStateException("O eleitor ainda não votou em todos os cargos");
        }
        List<RegistroVoto> registros = sessao.getVotosConfirmados().stream().map(Voto::paraRegistro).toList();
        repositorio.salvar(registros);
    }

    public Apuracao apurar() throws PersistenciaException {
        return Apuracao.apurar(eleicao, repositorio.listarTodos());
    }

    public void zerarUrna() throws PersistenciaException {
        repositorio.zerar();
    }
}
