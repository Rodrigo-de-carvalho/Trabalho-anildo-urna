package br.com.urnacidada.servico;

import br.com.urnacidada.Fixtures;
import br.com.urnacidada.modelo.TipoVoto;
import br.com.urnacidada.modelo.Voto;
import br.com.urnacidada.modelo.VotoNominal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessaoVotacaoTest {

    private SessaoVotacao sessao;

    @BeforeEach
    void setUp() {
        sessao = new SessaoVotacao(Fixtures.eleicao());
    }

    private void digitar(String numero) {
        numero.chars().forEach(c -> sessao.digitar(c - '0'));
    }

    @Test
    void votaEmVereadorEPrefeitoNaOrdem() {
        assertEquals("Vereador", sessao.getCargoAtual().orElseThrow().getNome());
        digitar("14111");
        Voto vereador = sessao.confirmar().orElseThrow();
        assertEquals(TipoVoto.NOMINAL, vereador.getTipo());
        assertEquals("Félix Ronron", ((VotoNominal) vereador).getCandidato().getNomeUrna());

        assertEquals("Prefeito", sessao.getCargoAtual().orElseThrow().getNome());
        digitar("27");
        Voto prefeito = sessao.confirmar().orElseThrow();
        assertEquals(TipoVoto.NOMINAL, prefeito.getTipo());
        assertTrue(sessao.isFinalizada());
        assertEquals(2, sessao.getVotosConfirmados().size());
    }

    @Test
    void naoConfirmaComNumeroIncompleto() {
        digitar("141");
        assertTrue(sessao.confirmar().isEmpty());
        assertEquals("Vereador", sessao.getCargoAtual().orElseThrow().getNome());
    }

    @Test
    void ignoraDigitosAlemDoTamanhoDoCargo() {
        digitar("1411199");
        assertEquals("14111", sessao.getDigitos());
    }

    @Test
    void numeroDoPartidoNoProporcionalEhVotoDeLegenda() {
        digitar("38");
        assertEquals("PLL", sessao.getPartidoDigitado().orElseThrow().getSigla());
        Voto voto = sessao.confirmar().orElseThrow();
        assertEquals(TipoVoto.LEGENDA, voto.getTipo());
        assertEquals("38", voto.getNumeroRegistrado());
    }

    @Test
    void candidatoInexistenteDePartidoExistenteViraLegenda() {
        digitar("46999");
        Voto voto = sessao.getVotoEmPreparacao().orElseThrow();
        assertEquals(TipoVoto.LEGENDA, voto.getTipo());
        assertEquals("CANDIDATO INEXISTENTE - VOTO DE LEGENDA", voto.getMensagemTela());
    }

    @Test
    void partidoInexistenteNoProporcionalEhNulo() {
        digitar("99123");
        assertEquals(TipoVoto.NULO, sessao.confirmar().orElseThrow().getTipo());
    }

    @Test
    void majoritarioSemCandidatoEhNuloMesmoComPartidoExistente() {
        digitar("14111");
        sessao.confirmar();
        digitar("59"); // o Partido do Esporte não lançou candidato a prefeito
        Voto voto = sessao.confirmar().orElseThrow();
        assertEquals(TipoVoto.NULO, voto.getTipo());
        assertEquals("", voto.getNumeroRegistrado());
    }

    @Test
    void brancoSoFuncionaSemDigitosEBloqueiaNumeros() {
        digitar("1");
        assertFalse(sessao.votarEmBranco());
        sessao.corrigir();
        assertTrue(sessao.votarEmBranco());
        assertFalse(sessao.digitar(5));
        assertEquals(TipoVoto.BRANCO, sessao.confirmar().orElseThrow().getTipo());
    }

    @Test
    void corrigeLimpaDigitosEBranco() {
        digitar("27");
        sessao.corrigir();
        assertEquals("", sessao.getDigitos());
        sessao.votarEmBranco();
        sessao.corrigir();
        assertFalse(sessao.isBrancoSelecionado());
    }

    @Test
    void sessaoFinalizadaIgnoraTeclas() {
        sessao.votarEmBranco();
        sessao.confirmar();
        sessao.votarEmBranco();
        sessao.confirmar();
        assertTrue(sessao.isFinalizada());
        assertFalse(sessao.digitar(1));
        assertFalse(sessao.votarEmBranco());
        assertTrue(sessao.confirmar().isEmpty());
    }
}
