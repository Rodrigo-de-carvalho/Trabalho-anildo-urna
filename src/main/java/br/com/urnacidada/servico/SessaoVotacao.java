package br.com.urnacidada.servico;

import br.com.urnacidada.modelo.Candidato;
import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.Eleicao;
import br.com.urnacidada.modelo.Partido;
import br.com.urnacidada.modelo.Voto;
import br.com.urnacidada.modelo.VotoBranco;
import br.com.urnacidada.modelo.VotoLegenda;
import br.com.urnacidada.modelo.VotoNominal;
import br.com.urnacidada.modelo.VotoNulo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Votação de um único eleitor: percorre os cargos na ordem definida pela eleição,
 * reproduzindo as regras das teclas da urna eletrônica (números, BRANCO, CORRIGE e CONFIRMA).
 * Não depende de interface gráfica, o que permite testá-la isoladamente.
 */
public class SessaoVotacao {

    private final Eleicao eleicao;
    private final List<Voto> votosConfirmados = new ArrayList<>();
    private final StringBuilder digitos = new StringBuilder();
    private int indiceCargo;
    private boolean brancoSelecionado;

    public SessaoVotacao(Eleicao eleicao) {
        this.eleicao = Objects.requireNonNull(eleicao, "eleicao");
    }

    public boolean isFinalizada() {
        return indiceCargo >= eleicao.getCargos().size();
    }

    /** Cargo em votação no momento, ou vazio se a sessão terminou. */
    public Optional<Cargo> getCargoAtual() {
        return isFinalizada() ? Optional.empty() : Optional.of(eleicao.getCargos().get(indiceCargo));
    }

    public String getDigitos() {
        return digitos.toString();
    }

    public boolean isBrancoSelecionado() {
        return brancoSelecionado;
    }

    /** Tecla numérica. Retorna {@code false} quando a tecla é ignorada. */
    public boolean digitar(int digito) {
        if (digito < 0 || digito > 9) {
            throw new IllegalArgumentException("Dígito inválido: " + digito);
        }
        Optional<Cargo> cargo = getCargoAtual();
        if (cargo.isEmpty() || brancoSelecionado || digitos.length() >= cargo.get().getQuantidadeDigitos()) {
            return false;
        }
        digitos.append(digito);
        return true;
    }

    /** Tecla CORRIGE: limpa o que foi digitado para o cargo atual. */
    public void corrigir() {
        digitos.setLength(0);
        brancoSelecionado = false;
    }

    /** Tecla BRANCO: só funciona antes de digitar qualquer número (como na urna real). */
    public boolean votarEmBranco() {
        if (isFinalizada() || digitos.length() > 0) {
            return false;
        }
        brancoSelecionado = true;
        return true;
    }

    /** Partido correspondente aos dois primeiros dígitos, se já digitados e existentes. */
    public Optional<Partido> getPartidoDigitado() {
        if (digitos.length() < 2) {
            return Optional.empty();
        }
        return eleicao.buscarPartido(digitos.substring(0, 2));
    }

    /**
     * O voto que seria registrado se o eleitor apertasse CONFIRMA agora.
     * Vazio enquanto o número estiver incompleto.
     */
    public Optional<Voto> getVotoEmPreparacao() {
        Optional<Cargo> talvezCargo = getCargoAtual();
        if (talvezCargo.isEmpty()) {
            return Optional.empty();
        }
        Cargo cargo = talvezCargo.get();
        if (brancoSelecionado) {
            return Optional.of(new VotoBranco(cargo));
        }
        String numero = digitos.toString();
        Optional<Partido> partido = getPartidoDigitado();

        if (numero.length() == cargo.getQuantidadeDigitos()) {
            Optional<Candidato> candidato = eleicao.buscarCandidato(cargo, numero);
            if (candidato.isPresent()) {
                return Optional.of(new VotoNominal(candidato.get()));
            }
            if (cargo.permiteVotoLegenda() && partido.isPresent()) {
                return Optional.of(new VotoLegenda(cargo, partido.get(), true));
            }
            return Optional.of(new VotoNulo(cargo));
        }
        // No proporcional, digitar só o número do partido e confirmar é voto de legenda.
        if (numero.length() == 2 && cargo.permiteVotoLegenda() && partido.isPresent()) {
            return Optional.of(new VotoLegenda(cargo, partido.get(), false));
        }
        return Optional.empty();
    }

    /**
     * Tecla CONFIRMA: registra o voto do cargo atual e avança para o próximo.
     * Retorna o voto confirmado, ou vazio se ainda não havia voto completo.
     */
    public Optional<Voto> confirmar() {
        Optional<Voto> voto = getVotoEmPreparacao();
        voto.ifPresent(v -> {
            votosConfirmados.add(v);
            indiceCargo++;
            corrigir();
        });
        return voto;
    }

    public List<Voto> getVotosConfirmados() {
        return Collections.unmodifiableList(votosConfirmados);
    }
}
