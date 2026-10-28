package br.com.urnacidada.servico;

import br.com.urnacidada.modelo.Candidato;
import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.Partido;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Totais apurados para um cargo. */
public class ResultadoCargo {

    /** Linha do boletim: um candidato ou um partido (legenda) com seus votos. */
    public record Linha(String numero, String nome, String partido, long votos) {
    }

    private final Cargo cargo;
    private final Map<Candidato, Long> votosPorCandidato = new LinkedHashMap<>();
    private final Map<Partido, Long> votosDeLegenda = new LinkedHashMap<>();
    private long brancos;
    private long nulos;

    ResultadoCargo(Cargo cargo, List<Candidato> candidatos, List<Partido> partidos) {
        this.cargo = cargo;
        candidatos.forEach(c -> votosPorCandidato.put(c, 0L));
        if (cargo.permiteVotoLegenda()) {
            partidos.forEach(p -> votosDeLegenda.put(p, 0L));
        }
    }

    void contarCandidato(Candidato candidato) {
        votosPorCandidato.merge(candidato, 1L, Long::sum);
    }

    void contarLegenda(Partido partido) {
        votosDeLegenda.merge(partido, 1L, Long::sum);
    }

    void contarBranco() {
        brancos++;
    }

    void contarNulo() {
        nulos++;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public long getBrancos() {
        return brancos;
    }

    public long getNulos() {
        return nulos;
    }

    public long getVotosValidos() {
        return soma(votosPorCandidato) + soma(votosDeLegenda);
    }

    public long getTotal() {
        return getVotosValidos() + brancos + nulos;
    }

    /** Candidatos ordenados do mais votado para o menos votado. */
    public List<Linha> getLinhasCandidatos() {
        List<Linha> linhas = new ArrayList<>();
        votosPorCandidato.forEach((c, v) ->
                linhas.add(new Linha(c.getNumero(), c.getNomeUrna(), c.getPartido().getSigla(), v)));
        linhas.sort(Comparator.comparingLong(Linha::votos).reversed());
        return linhas;
    }

    /** Votos de legenda por partido (vazio em cargos majoritários). */
    public List<Linha> getLinhasLegenda() {
        List<Linha> linhas = new ArrayList<>();
        votosDeLegenda.forEach((p, v) ->
                linhas.add(new Linha(p.getNumeroFormatado(), p.getNome(), p.getSigla(), v)));
        linhas.sort(Comparator.comparingLong(Linha::votos).reversed());
        return linhas;
    }

    /**
     * Votos totais de cada partido (nominais dos seus candidatos + legenda). No sistema
     * proporcional é esse total que define quantas cadeiras cada partido conquista.
     */
    public Map<Partido, Long> getVotosPorPartido() {
        Map<Partido, Long> total = new LinkedHashMap<>(votosDeLegenda);
        votosPorCandidato.forEach((c, v) -> total.merge(c.getPartido(), v, Long::sum));
        return total;
    }

    /** Percentual sobre os votos válidos (brancos e nulos não entram na conta). */
    public double percentualValidos(long votos) {
        long validos = getVotosValidos();
        return validos == 0 ? 0.0 : votos * 100.0 / validos;
    }

    private static long soma(Map<?, Long> mapa) {
        return mapa.values().stream().mapToLong(Long::longValue).sum();
    }
}
