package br.com.urnacidada.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Configurações da urna, lidas (em ordem de prioridade) de variáveis de ambiente
 * e do arquivo {@code urna.properties} na pasta onde o programa é executado.
 *
 * <pre>
 * firebase.credenciais = caminho do JSON da conta de serviço (ou GOOGLE_APPLICATION_CREDENTIALS)
 * firebase.colecao     = nome da coleção no Firestore (padrão: votos)
 * arquivo.votos        = arquivo local de votos (padrão: votos-locais.json)
 * eleicao.arquivo      = JSON com cargos/partidos/candidatos (padrão: o embutido no programa)
 * mesario.senha        = senha para apuração e zerar a urna (padrão: 1234)
 * </pre>
 */
public final class Configuracao {

    public static final String ARQUIVO_PADRAO = "urna.properties";

    private final Properties propriedades;

    private Configuracao(Properties propriedades) {
        this.propriedades = propriedades;
    }

    public static Configuracao carregar() {
        return carregar(Path.of(ARQUIVO_PADRAO));
    }

    public static Configuracao carregar(Path arquivo) {
        Properties p = new Properties();
        if (Files.exists(arquivo)) {
            try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                p.load(leitor);
            } catch (IOException e) {
                System.err.println("Aviso: não foi possível ler " + arquivo + ": " + e.getMessage());
            }
        }
        return new Configuracao(p);
    }

    public Optional<Path> getCredenciaisFirebase() {
        return valor("firebase.credenciais", "GOOGLE_APPLICATION_CREDENTIALS").map(Path::of);
    }

    public String getColecaoFirestore() {
        return valor("firebase.colecao", "URNA_COLECAO").orElse("votos");
    }

    public Path getArquivoVotosLocais() {
        return Path.of(valor("arquivo.votos", "URNA_ARQUIVO_VOTOS").orElse("votos-locais.json"));
    }

    public Optional<Path> getArquivoEleicao() {
        return valor("eleicao.arquivo", "URNA_ELEICAO").map(Path::of);
    }

    public String getSenhaMesario() {
        return valor("mesario.senha", "URNA_SENHA").orElse("1234");
    }

    private Optional<String> valor(String chave, String variavelAmbiente) {
        String ambiente = System.getenv(variavelAmbiente);
        if (ambiente != null && !ambiente.isBlank()) {
            return Optional.of(ambiente.trim());
        }
        String propriedade = propriedades.getProperty(chave);
        if (propriedade != null && !propriedade.isBlank()) {
            return Optional.of(propriedade.trim());
        }
        return Optional.empty();
    }
}
