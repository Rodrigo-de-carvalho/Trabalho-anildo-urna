package br.com.urnacidada.config;

import br.com.urnacidada.modelo.CandidatoMajoritario;
import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.Eleicao;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarregadorEleicaoTest {

    @Test
    void carregaEleicaoPadrao() {
        Eleicao eleicao = CarregadorEleicao.carregarPadrao();
        assertEquals(2, eleicao.getCargos().size());
        Cargo vereador = eleicao.getCargos().get(0);
        Cargo prefeito = eleicao.getCargos().get(1);
        assertTrue(vereador.permiteVotoLegenda());
        assertEquals(5, vereador.getQuantidadeDigitos());
        assertFalse(prefeito.permiteVotoLegenda());
        assertEquals(10, eleicao.getCandidatos(vereador).size());
        assertEquals(4, eleicao.getCandidatos(prefeito).size());
        CandidatoMajoritario mia = (CandidatoMajoritario) eleicao.buscarCandidato(prefeito, "14").orElseThrow();
        assertEquals("Tom Novelo", mia.getNomeVice());
    }

    @Test
    void rejeitaCandidatoComNumeroQueNaoComecaPeloPartido() {
        String json = """
                {"cargos":[{"nome":"Prefeito","sistema":"MAJORITARIO"}],
                 "partidos":[{"numero":10,"sigla":"A","nome":"A"},{"numero":20,"sigla":"B","nome":"B"}],
                 "candidatos":[{"cargo":"Prefeito","numero":"10","nome":"X","partido":20}]}""";
        assertThrows(IllegalArgumentException.class, () -> CarregadorEleicao.carregar(new StringReader(json)));
    }

    @Test
    void rejeitaNumeroComQuantidadeErradaDeDigitos() {
        String json = """
                {"cargos":[{"nome":"Vereador","sistema":"PROPORCIONAL","digitos":5}],
                 "partidos":[{"numero":10,"sigla":"A","nome":"A"}],
                 "candidatos":[{"cargo":"Vereador","numero":"1012","nome":"X"}]}""";
        assertThrows(IllegalArgumentException.class, () -> CarregadorEleicao.carregar(new StringReader(json)));
    }

    @Test
    void rejeitaCargoDesconhecido() {
        String json = """
                {"cargos":[{"nome":"Prefeito","sistema":"MAJORITARIO"}],
                 "partidos":[{"numero":10,"sigla":"A","nome":"A"}],
                 "candidatos":[{"cargo":"Governador","numero":"10","nome":"X"}]}""";
        assertThrows(IllegalArgumentException.class, () -> CarregadorEleicao.carregar(new StringReader(json)));
    }
}
