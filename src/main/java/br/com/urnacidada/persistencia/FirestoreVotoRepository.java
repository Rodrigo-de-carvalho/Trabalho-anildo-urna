package br.com.urnacidada.persistencia;

import br.com.urnacidada.modelo.RegistroVoto;
import br.com.urnacidada.modelo.TipoVoto;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Persiste os votos no Cloud Firestore (Firebase).
 *
 * <p>Modelo de dados: coleção {@code votos} (nome configurável), um documento por voto,
 * com ID aleatório gerado pelo Firestore e os campos {@code cargo}, {@code tipo} e
 * {@code numero}. Não há identificação do eleitor nem horário, preservando o sigilo.
 */
public class FirestoreVotoRepository implements VotoRepository {

    private static final long TEMPO_LIMITE_SEGUNDOS = 15;
    private static final int LIMITE_LOTE = 400; // o Firestore aceita até 500 operações por lote

    private final Firestore firestore;
    private final String nomeColecao;

    public FirestoreVotoRepository(Firestore firestore, String nomeColecao) {
        this.firestore = firestore;
        this.nomeColecao = nomeColecao;
    }

    /** Inicializa o Firebase a partir do arquivo JSON da conta de serviço. */
    public static FirestoreVotoRepository conectar(Path credenciais, String nomeColecao)
            throws PersistenciaException {
        try (InputStream entrada = Files.newInputStream(credenciais)) {
            FirebaseOptions opcoes = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(entrada))
                    .build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(opcoes)
                    : FirebaseApp.getInstance();
            return new FirestoreVotoRepository(FirestoreClient.getFirestore(app), nomeColecao);
        } catch (IOException | RuntimeException e) {
            throw new PersistenciaException("Falha ao conectar no Firebase com " + credenciais
                    + ": " + e.getMessage(), e);
        }
    }

    private CollectionReference colecao() {
        return firestore.collection(nomeColecao);
    }

    @Override
    public void salvar(List<RegistroVoto> votosDoEleitor) throws PersistenciaException {
        // Lote atômico: ou todos os votos do eleitor são gravados, ou nenhum.
        WriteBatch lote = firestore.batch();
        for (RegistroVoto voto : votosDoEleitor) {
            lote.set(colecao().document(), Map.of(
                    "cargo", voto.cargo(),
                    "tipo", voto.tipo().name(),
                    "numero", voto.numero()));
        }
        aguardar(lote.commit(), "gravar os votos");
    }

    @Override
    public List<RegistroVoto> listarTodos() throws PersistenciaException {
        QuerySnapshot consulta = aguardar(colecao().get(), "ler os votos");
        List<RegistroVoto> votos = new ArrayList<>();
        for (QueryDocumentSnapshot doc : consulta.getDocuments()) {
            String cargo = doc.getString("cargo");
            String tipo = doc.getString("tipo");
            if (cargo == null || tipo == null) {
                continue;
            }
            try {
                votos.add(new RegistroVoto(cargo, TipoVoto.valueOf(tipo), doc.getString("numero")));
            } catch (IllegalArgumentException ignorado) {
                // documento com tipo desconhecido: não entra na apuração
            }
        }
        return votos;
    }

    @Override
    public void zerar() throws PersistenciaException {
        List<QueryDocumentSnapshot> documentos = aguardar(colecao().get(), "ler os votos").getDocuments();
        for (int i = 0; i < documentos.size(); i += LIMITE_LOTE) {
            WriteBatch lote = firestore.batch();
            documentos.subList(i, Math.min(i + LIMITE_LOTE, documentos.size()))
                    .forEach(doc -> lote.delete(doc.getReference()));
            aguardar(lote.commit(), "apagar os votos");
        }
    }

    @Override
    public String getDescricao() {
        return "Firebase (Cloud Firestore)";
    }

    private static <T> T aguardar(ApiFuture<T> futuro, String acao) throws PersistenciaException {
        try {
            return futuro.get(TEMPO_LIMITE_SEGUNDOS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenciaException("Operação interrompida ao " + acao, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new PersistenciaException("Erro no Firebase ao " + acao + ": " + e.getMessage(), e);
        }
    }
}
