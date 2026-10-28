package br.com.urnacidada.ui;

import br.com.urnacidada.modelo.Candidato;
import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.Eleicao;

/** Textos educativos exibidos no menu Ajuda. */
final class TextosAjuda {

    static final String COMO_VOTAR = """
            COMO VOTAR NA URNA ELETRÔNICA

            1. Digite o número do candidato. A foto, o nome e o partido aparecem na tela.
            2. Confira os dados e aperte CONFIRMA (verde).
            3. Errou? Aperte CORRIGE (laranja) e digite de novo.
            4. Para votar em BRANCO, aperte a tecla BRANCO antes de digitar qualquer número.

            O que cada situação significa:
            • Voto nominal: número de um candidato que existe.
            • Voto de legenda (só no cargo proporcional, ex.: vereador): digitar apenas o
              número do partido (2 dígitos) e confirmar, ou um número de candidato que não
              existe, mas cujo partido existe. O voto vai para o partido.
            • Voto nulo: número que não corresponde a candidato (nem a partido no proporcional).
            • Brancos e nulos NÃO são votos válidos: não vão para nenhum candidato.

            A votação termina quando aparece FIM na tela, com o bipe longo.""";

    static final String SOBRE = """
            URNA CIDADÃ - Simulação educativa do voto eletrônico

            Projeto de Extensão Curricular - Ciência da Computação (POO)
            Java + Swing + Firebase (Cloud Firestore)

            Todos os partidos e candidatos são FICTÍCIOS e não têm qualquer vínculo com
            legendas ou pessoas reais. A atividade é apartidária.

            Os votos são gravados de forma anônima: sem nome do eleitor, sem horário e
            sem ordem, assim como acontece na urna eletrônica real.

            Desconfie de vídeos, áudios e imagens sobre eleições que circulam sem fonte.
            Verifique em canais oficiais (TSE / TREs) e agências de checagem.""";

    private TextosAjuda() {
    }

    static String listaCandidatos(Eleicao eleicao) {
        StringBuilder sb = new StringBuilder();
        for (Cargo cargo : eleicao.getCargos()) {
            sb.append(cargo.getNome().toUpperCase()).append(" (").append(cargo.getSistemaEleitoral())
                    .append(", ").append(cargo.getQuantidadeDigitos()).append(" dígitos)\n");
            for (Candidato c : eleicao.getCandidatos(cargo)) {
                sb.append("   ").append(c.getNumero()).append("  ").append(c.getNomeUrna())
                        .append("  -  ").append(c.getPartido().getSigla());
                c.getInformacoesAdicionais().forEach(info -> sb.append("  (").append(info).append(')'));
                sb.append('\n');
            }
            sb.append('\n');
        }
        sb.append("PARTIDOS\n");
        eleicao.getPartidos().forEach(p ->
                sb.append("   ").append(p.getNumero()).append("  ").append(p.getSigla())
                        .append(" - ").append(p.getNome()).append('\n'));
        return sb.toString();
    }
}
