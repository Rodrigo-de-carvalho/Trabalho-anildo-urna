package br.com.urnacidada.servico;

import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.Eleicao;
import br.com.urnacidada.modelo.RegistroVoto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Totaliza os registros de voto gravados, gerando o boletim de urna. */
public final class Apuracao {

    private final Map<Cargo, ResultadoCargo> resultados = new LinkedHashMap<>();
    private int registrosIgnorados;

    private Apuracao() {
    }

    public static Apuracao apurar(Eleicao eleicao, List<RegistroVoto> registros) {
        Apuracao apuracao = new Apuracao();
        for (Cargo cargo : eleicao.getCargos()) {
            apuracao.resultados.put(cargo,
                    new ResultadoCargo(cargo, eleicao.getCandidatos(cargo), eleicao.getPartidos()));
        }
        for (RegistroVoto registro : registros) {
            if (!apuracao.contabilizar(eleicao, registro)) {
                apuracao.registrosIgnorados++;
            }
        }
        return apuracao;
    }

    private boolean contabilizar(Eleicao eleicao, RegistroVoto registro) {
        Cargo cargo = eleicao.buscarCargo(registro.cargo()).orElse(null);
        if (cargo == null) {
            return false;
        }
        ResultadoCargo resultado = resultados.get(cargo);
        switch (registro.tipo()) {
            case BRANCO -> resultado.contarBranco();
            case NULO -> resultado.contarNulo();
            case NOMINAL -> {
                var candidato = eleicao.buscarCandidato(cargo, registro.numero());
                if (candidato.isEmpty()) {
                    return false;
                }
                resultado.contarCandidato(candidato.get());
            }
            case LEGENDA -> {
                var partido = eleicao.buscarPartido(registro.numero());
                if (partido.isEmpty() || !cargo.permiteVotoLegenda()) {
                    return false;
                }
                resultado.contarLegenda(partido.get());
            }
        }
        return true;
    }

    public List<ResultadoCargo> getResultados() {
        return new ArrayList<>(resultados.values());
    }

    public ResultadoCargo getResultado(Cargo cargo) {
        return resultados.get(cargo);
    }

    /** Quantos eleitores votaram (todos votam em todos os cargos). */
    public long getComparecimento() {
        return resultados.values().stream().mapToLong(ResultadoCargo::getTotal).max().orElse(0);
    }

    /**
     * Registros que não correspondem à eleição carregada (ex.: candidato removido do
     * arquivo de configuração depois da votação). Não entram em nenhum total.
     */
    public int getRegistrosIgnorados() {
        return registrosIgnorados;
    }
}
