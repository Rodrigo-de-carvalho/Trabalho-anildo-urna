package br.com.urnacidada;

import br.com.urnacidada.config.CarregadorEleicao;
import br.com.urnacidada.modelo.Eleicao;

/** Dados de teste compartilhados: a eleição padrão embutida no programa. */
public final class Fixtures {

    private Fixtures() {
    }

    public static Eleicao eleicao() {
        return CarregadorEleicao.carregarPadrao();
    }
}
