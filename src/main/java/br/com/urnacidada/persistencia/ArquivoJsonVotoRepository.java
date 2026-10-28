package br.com.urnacidada.persistencia;

import br.com.urnacidada.modelo.RegistroVoto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Guarda os votos em um arquivo JSON local. Usado quando não há Firebase configurado
 * ou como reserva quando a internet cai durante a oficina.
 */
public class ArquivoJsonVotoRepository implements VotoRepository {

    private static final TypeToken<List<RegistroVoto>> TIPO_LISTA = new TypeToken<>() {
    };

    private final Path arquivo;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ArquivoJsonVotoRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    public Path getArquivo() {
        return arquivo;
    }

    @Override
    public synchronized void salvar(List<RegistroVoto> votosDoEleitor) throws PersistenciaException {
        List<RegistroVoto> todos = new ArrayList<>(ler());
        todos.addAll(votosDoEleitor);
        // Embaralha para que a ordem do arquivo não revele a ordem dos eleitores (sigilo do voto).
        Collections.shuffle(todos);
        escrever(todos);
    }

    @Override
    public synchronized List<RegistroVoto> listarTodos() throws PersistenciaException {
        return List.copyOf(ler());
    }

    @Override
    public synchronized void zerar() throws PersistenciaException {
        escrever(List.of());
    }

    @Override
    public String getDescricao() {
        return "Arquivo local (" + arquivo.getFileName() + ")";
    }

    private List<RegistroVoto> ler() throws PersistenciaException {
        if (!Files.exists(arquivo)) {
            return List.of();
        }
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            List<RegistroVoto> votos = gson.fromJson(leitor, TIPO_LISTA);
            return votos == null ? List.of() : votos;
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("Não foi possível ler " + arquivo, e);
        }
    }

    private void escrever(List<RegistroVoto> votos) throws PersistenciaException {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            // Grava num arquivo temporário e troca no fim, para não corromper os votos se o PC desligar.
            Path temporario = Files.createTempFile(pasta, "votos", ".tmp");
            try (Writer escritor = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                gson.toJson(votos, TIPO_LISTA.getType(), escritor);
            }
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar " + arquivo, e);
        }
    }
}
