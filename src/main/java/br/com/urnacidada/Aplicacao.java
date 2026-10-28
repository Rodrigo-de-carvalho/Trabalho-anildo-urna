package br.com.urnacidada;

import br.com.urnacidada.config.CarregadorEleicao;
import br.com.urnacidada.config.Configuracao;
import br.com.urnacidada.modelo.Eleicao;
import br.com.urnacidada.persistencia.ArquivoJsonVotoRepository;
import br.com.urnacidada.persistencia.FirestoreVotoRepository;
import br.com.urnacidada.persistencia.PersistenciaException;
import br.com.urnacidada.persistencia.VotoRepository;
import br.com.urnacidada.persistencia.VotoRepositoryComReserva;
import br.com.urnacidada.servico.ServicoUrna;
import br.com.urnacidada.ui.JanelaUrna;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/** Ponto de entrada: lê a configuração, escolhe onde gravar os votos e abre a urna. */
public final class Aplicacao {

    private Aplicacao() {
    }

    public static void main(String[] args) {
        Configuracao config = Configuracao.carregar();
        Eleicao eleicao;
        try {
            eleicao = carregarEleicao(config);
        } catch (IOException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Erro na configuração da eleição:\n" + e.getMessage(),
                    "Urna Cidadã", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignorado) {
                // segue com o visual padrão
            }
            AvisoTardio avisos = new AvisoTardio();
            ServicoUrna servico = new ServicoUrna(eleicao, criarRepositorio(config, avisos));
            JanelaUrna janela = new JanelaUrna(servico, config.getSenhaMesario());
            avisos.conectar(janela::avisar);
            janela.setVisible(true);
        });
    }

    private static Eleicao carregarEleicao(Configuracao config) throws IOException {
        Optional<Path> arquivo = config.getArquivoEleicao();
        return arquivo.isPresent() ? CarregadorEleicao.carregar(arquivo.get()) : CarregadorEleicao.carregarPadrao();
    }

    /**
     * Com credenciais do Firebase: grava na nuvem, com o arquivo local como reserva.
     * Sem credenciais (ou se a conexão falhar): grava só no arquivo local.
     */
    static VotoRepository criarRepositorio(Configuracao config, Consumer<String> avisos) {
        ArquivoJsonVotoRepository local = new ArquivoJsonVotoRepository(config.getArquivoVotosLocais());
        Optional<Path> credenciais = config.getCredenciaisFirebase();
        if (credenciais.isEmpty()) {
            System.out.println("Firebase não configurado: votos serão gravados em " + local.getArquivo());
            return local;
        }
        if (!Files.exists(credenciais.get())) {
            avisos.accept("Arquivo de credenciais do Firebase não encontrado (" + credenciais.get()
                    + "). Usando apenas o arquivo local.");
            return local;
        }
        try {
            FirestoreVotoRepository nuvem = FirestoreVotoRepository.conectar(credenciais.get(),
                    config.getColecaoFirestore());
            return new VotoRepositoryComReserva(nuvem, local, avisos);
        } catch (PersistenciaException e) {
            avisos.accept(e.getMessage() + " Usando apenas o arquivo local.");
            return local;
        }
    }

    /** Guarda avisos gerados antes de a janela existir e os repassa quando ela abrir. */
    private static final class AvisoTardio implements Consumer<String> {
        private Consumer<String> destino;
        private String pendente;

        @Override
        public synchronized void accept(String mensagem) {
            System.err.println("Aviso: " + mensagem);
            if (destino != null) {
                destino.accept(mensagem);
            } else {
                pendente = mensagem;
            }
        }

        synchronized void conectar(Consumer<String> destino) {
            this.destino = destino;
            if (pendente != null) {
                destino.accept(pendente);
                pendente = null;
            }
        }
    }
}
