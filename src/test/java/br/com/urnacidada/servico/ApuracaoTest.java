package br.com.urnacidada.servico;

import br.com.urnacidada.Fixtures;
import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.Eleicao;
import br.com.urnacidada.modelo.RegistroVoto;
import br.com.urnacidada.modelo.TipoVoto;
import br.com.urnacidada.persistencia.MemoriaVotoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApuracaoTest {

    private final Eleicao eleicao = Fixtures.eleicao();

    private void votar(ServicoUrna servico, String vereador, String prefeito) throws Exception {
        SessaoVotacao sessao = servico.novaSessao();
        for (String numero : new String[]{vereador, prefeito}) {
            if (numero == null) {
                sessao.votarEmBranco();
            } else {
                numero.chars().forEach(c -> sessao.digitar(c - '0'));
            }
            sessao.confirmar();
        }
        servico.registrar(sessao);
    }

    @Test
    void totalizaVotosPorTipoECandidato() throws Exception {
        ServicoUrna servico = new ServicoUrna(eleicao, new MemoriaVotoRepository());
        votar(servico, "14111", "14");
        votar(servico, "14111", "27");
        votar(servico, "14", "27");     // legenda PGT
        votar(servico, null, "99");     // branco / nulo
        votar(servico, "27222", null);  // nominal / branco

        Apuracao apuracao = servico.apurar();
        assertEquals(5, apuracao.getComparecimento());

        Cargo vereador = eleicao.buscarCargo("Vereador").orElseThrow();
        ResultadoCargo rv = apuracao.getResultado(vereador);
        assertEquals(4, rv.getVotosValidos());
        assertEquals(1, rv.getBrancos());
        assertEquals(0, rv.getNulos());
        assertEquals(new ResultadoCargo.Linha("14111", "Félix Ronron", "PGT", 2), rv.getLinhasCandidatos().get(0));
        // PGT: 2 nominais + 1 legenda
        assertEquals(3L, rv.getVotosPorPartido().get(eleicao.buscarPartido(14).orElseThrow()));
        assertEquals(75.0, rv.percentualValidos(3), 0.001);

        ResultadoCargo rp = apuracao.getResultado(eleicao.buscarCargo("Prefeito").orElseThrow());
        assertEquals(3, rp.getVotosValidos());
        assertEquals(1, rp.getBrancos());
        assertEquals(1, rp.getNulos());
        assertEquals("Rex Latido", rp.getLinhasCandidatos().get(0).nome());
        assertEquals(0, rp.getLinhasLegenda().size());
    }

    @Test
    void ignoraRegistrosQueNaoPertencemAEleicao() {
        Apuracao apuracao = Apuracao.apurar(eleicao, List.of(
                new RegistroVoto("Senador", TipoVoto.BRANCO, ""),
                new RegistroVoto("Prefeito", TipoVoto.NOMINAL, "77"),
                new RegistroVoto("Prefeito", TipoVoto.LEGENDA, "14"),
                new RegistroVoto("Prefeito", TipoVoto.NOMINAL, "46")));
        assertEquals(3, apuracao.getRegistrosIgnorados());
        assertEquals(1, apuracao.getComparecimento());
    }

    @Test
    void percentualSemVotosValidosEhZero() {
        Apuracao apuracao = Apuracao.apurar(eleicao, List.of());
        apuracao.getResultados().forEach(r -> assertEquals(0.0, r.percentualValidos(0)));
    }
}
