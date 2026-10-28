package br.com.urnacidada.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Bipes da urna, gerados na hora (sem arquivos de áudio). */
final class Som {

    private static final float TAXA = 22050f;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "som-urna");
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean ativo = true;

    private Som() {
    }

    static void setAtivo(boolean ativo) {
        Som.ativo = ativo;
    }

    static boolean isAtivo() {
        return ativo;
    }

    /** Bipe curto de tecla. */
    static void tecla() {
        tocar(new int[][]{{1000, 60}});
    }

    /** Confirmação de um cargo. */
    static void confirma() {
        tocar(new int[][]{{1000, 90}, {0, 40}, {1000, 90}});
    }

    /** O famoso "pi-pi-pi-pi-piiii" do fim da votação. */
    static void fim() {
        tocar(new int[][]{{1000, 110}, {0, 50}, {1000, 110}, {0, 50}, {1000, 110}, {0, 50}, {1000, 110}, {0, 50},
                {1000, 700}});
    }

    /** Tecla que não faz nada no momento. */
    static void erro() {
        tocar(new int[][]{{400, 150}});
    }

    /** Cada par é {frequência em Hz (0 = silêncio), duração em ms}. */
    private static void tocar(int[][] notas) {
        if (!ativo) {
            return;
        }
        EXECUTOR.execute(() -> {
            try {
                AudioFormat formato = new AudioFormat(TAXA, 8, 1, true, false);
                try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
                    linha.open(formato);
                    linha.start();
                    for (int[] nota : notas) {
                        byte[] amostras = gerar(nota[0], nota[1]);
                        linha.write(amostras, 0, amostras.length);
                    }
                    linha.drain();
                }
            } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    private static byte[] gerar(int frequencia, int duracaoMs) {
        byte[] amostras = new byte[(int) (TAXA * duracaoMs / 1000)];
        if (frequencia > 0) {
            for (int i = 0; i < amostras.length; i++) {
                // Onda quadrada, parecida com o bipe da urna.
                double fase = (i * frequencia / TAXA) % 1.0;
                amostras[i] = (byte) (fase < 0.5 ? 40 : -40);
            }
        }
        return amostras;
    }
}
