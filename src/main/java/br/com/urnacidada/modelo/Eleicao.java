package br.com.urnacidada.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Agrega os dados de uma eleição simulada: cargos (na ordem de votação),
 * partidos e candidatos.
 */
public class Eleicao {

    private final String titulo;
    private final List<Cargo> cargos;
    private final Map<Integer, Partido> partidos = new LinkedHashMap<>();
    private final Map<String, Candidato> candidatos = new LinkedHashMap<>();

    public Eleicao(String titulo, List<Cargo> cargosNaOrdemDeVotacao) {
        this.titulo = Objects.requireNonNull(titulo, "titulo");
        if (cargosNaOrdemDeVotacao == null || cargosNaOrdemDeVotacao.isEmpty()) {
            throw new IllegalArgumentException("A eleição precisa de pelo menos um cargo");
        }
        this.cargos = List.copyOf(cargosNaOrdemDeVotacao);
    }

    public void adicionarPartido(Partido partido) {
        if (partidos.putIfAbsent(partido.getNumero(), partido) != null) {
            throw new IllegalArgumentException("Partido repetido: " + partido.getNumero());
        }
    }

    public void adicionarCandidato(Candidato candidato) {
        if (!cargos.contains(candidato.getCargo())) {
            throw new IllegalArgumentException("Cargo não faz parte da eleição: " + candidato.getCargo());
        }
        if (!partidos.containsKey(candidato.getPartido().getNumero())) {
            throw new IllegalArgumentException("Partido não cadastrado: " + candidato.getPartido());
        }
        if (candidatos.putIfAbsent(chave(candidato.getCargo(), candidato.getNumero()), candidato) != null) {
            throw new IllegalArgumentException("Já existe candidato " + candidato.getNumero()
                    + " para " + candidato.getCargo());
        }
    }

    public String getTitulo() {
        return titulo;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public Optional<Cargo> buscarCargo(String nome) {
        return cargos.stream().filter(c -> c.getNome().equalsIgnoreCase(nome)).findFirst();
    }

    public List<Partido> getPartidos() {
        return List.copyOf(partidos.values());
    }

    public Optional<Partido> buscarPartido(int numero) {
        return Optional.ofNullable(partidos.get(numero));
    }

    public Optional<Partido> buscarPartido(String numero) {
        try {
            return buscarPartido(Integer.parseInt(numero));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Candidato> buscarCandidato(Cargo cargo, String numero) {
        return Optional.ofNullable(candidatos.get(chave(cargo, numero)));
    }

    public List<Candidato> getCandidatos(Cargo cargo) {
        List<Candidato> lista = new ArrayList<>();
        for (Candidato c : candidatos.values()) {
            if (c.getCargo().equals(cargo)) {
                lista.add(c);
            }
        }
        return Collections.unmodifiableList(lista);
    }

    private static String chave(Cargo cargo, String numero) {
        return cargo.getNome().toLowerCase() + "#" + numero;
    }
}
