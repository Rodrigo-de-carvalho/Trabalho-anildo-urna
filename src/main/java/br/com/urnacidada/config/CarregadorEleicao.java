package br.com.urnacidada.config;

import br.com.urnacidada.modelo.CandidatoMajoritario;
import br.com.urnacidada.modelo.CandidatoProporcional;
import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.CargoMajoritario;
import br.com.urnacidada.modelo.CargoProporcional;
import br.com.urnacidada.modelo.Eleicao;
import br.com.urnacidada.modelo.Partido;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta uma {@link Eleicao} a partir de um arquivo JSON. Assim cada grupo pode
 * trocar partidos e candidatos fictícios sem mexer no código.
 */
public final class CarregadorEleicao {

    public static final String RECURSO_PADRAO = "/eleicao-padrao.json";

    // Estrutura do JSON (lida pelo Gson)
    private static final class Arquivo {
        String titulo;
        List<CargoJson> cargos;
        List<PartidoJson> partidos;
        List<CandidatoJson> candidatos;
    }

    private static final class CargoJson {
        String nome;
        String sistema;
        Integer digitos;
    }

    private static final class PartidoJson {
        int numero;
        String sigla;
        String nome;
        String cor;
    }

    private static final class CandidatoJson {
        String cargo;
        String numero;
        String nome;
        String vice;
        Integer partido;
    }

    private CarregadorEleicao() {
    }

    public static Eleicao carregarPadrao() {
        InputStream entrada = CarregadorEleicao.class.getResourceAsStream(RECURSO_PADRAO);
        if (entrada == null) {
            throw new IllegalStateException("Recurso " + RECURSO_PADRAO + " não encontrado");
        }
        try (Reader leitor = new InputStreamReader(entrada, StandardCharsets.UTF_8)) {
            return carregar(leitor);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler " + RECURSO_PADRAO, e);
        }
    }

    public static Eleicao carregar(Path arquivo) throws IOException {
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            return carregar(leitor);
        }
    }

    public static Eleicao carregar(Reader leitor) {
        Arquivo dados;
        try {
            dados = new Gson().fromJson(leitor, Arquivo.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("JSON da eleição inválido: " + e.getMessage(), e);
        }
        if (dados == null || dados.cargos == null || dados.partidos == null || dados.candidatos == null) {
            throw new IllegalArgumentException("O JSON da eleição precisa de 'cargos', 'partidos' e 'candidatos'");
        }

        List<Cargo> cargos = new ArrayList<>();
        for (CargoJson c : dados.cargos) {
            cargos.add(criarCargo(c));
        }
        Eleicao eleicao = new Eleicao(dados.titulo == null ? "Eleição Simulada" : dados.titulo, cargos);

        for (PartidoJson p : dados.partidos) {
            eleicao.adicionarPartido(new Partido(p.numero, p.sigla, p.nome, p.cor));
        }
        for (CandidatoJson c : dados.candidatos) {
            Cargo cargo = eleicao.buscarCargo(c.cargo)
                    .orElseThrow(() -> new IllegalArgumentException("Cargo desconhecido: " + c.cargo));
            if (c.numero == null || c.numero.length() < 2) {
                throw new IllegalArgumentException("Candidato sem número válido: " + c.nome);
            }
            // O partido vem dos 2 primeiros dígitos, a não ser que seja informado explicitamente.
            int numeroPartido = c.partido != null ? c.partido : Integer.parseInt(c.numero.substring(0, 2));
            Partido partido = eleicao.buscarPartido(numeroPartido)
                    .orElseThrow(() -> new IllegalArgumentException("Partido desconhecido: " + numeroPartido));

            if (cargo instanceof CargoMajoritario majoritario) {
                eleicao.adicionarCandidato(new CandidatoMajoritario(c.numero, c.nome,
                        c.vice == null ? "-" : c.vice, partido, majoritario));
            } else if (cargo instanceof CargoProporcional proporcional) {
                eleicao.adicionarCandidato(new CandidatoProporcional(c.numero, c.nome, partido, proporcional));
            }
        }
        return eleicao;
    }

    private static Cargo criarCargo(CargoJson c) {
        if (c.nome == null || c.sistema == null) {
            throw new IllegalArgumentException("Cada cargo precisa de 'nome' e 'sistema'");
        }
        return switch (c.sistema.toUpperCase()) {
            case "PROPORCIONAL" -> new CargoProporcional(c.nome, c.digitos == null ? 5 : c.digitos);
            case "MAJORITARIO", "MAJORITÁRIO" -> new CargoMajoritario(c.nome);
            default -> throw new IllegalArgumentException("Sistema eleitoral desconhecido: " + c.sistema);
        };
    }
}
