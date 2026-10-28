package br.com.urnacidada.ui;

import br.com.urnacidada.modelo.Cargo;
import br.com.urnacidada.modelo.Partido;
import br.com.urnacidada.modelo.Voto;
import br.com.urnacidada.modelo.VotoLegenda;
import br.com.urnacidada.modelo.VotoNominal;
import br.com.urnacidada.servico.SessaoVotacao;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Optional;

/** Tela de cristal líquido da urna, desenhada a partir do estado da {@link SessaoVotacao}. */
class PainelTela extends JPanel {

    enum Modo { VOTACAO, GRAVANDO, FIM }

    private static final Color FUNDO = new Color(0xF4F4EC);
    private static final Color TEXTO = new Color(0x1A1A1A);
    private static final Color DESTAQUE = new Color(0xC62828);
    private static final Font FONTE_PEQUENA = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private static final Font FONTE_MEDIA = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    private static final Font FONTE_CARGO = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    private static final Font FONTE_DIGITO = new Font(Font.MONOSPACED, Font.BOLD, 32);
    private static final Font FONTE_AVISO = new Font(Font.SANS_SERIF, Font.BOLD, 30);

    private SessaoVotacao sessao;
    private Modo modo = Modo.VOTACAO;
    private boolean cursorVisivel = true;

    PainelTela() {
        setBackground(FUNDO);
        setPreferredSize(new Dimension(660, 470));
        setBorder(BorderFactory.createMatteBorder(18, 18, 18, 18, new Color(0x3A3A3A)));
        // Cursor piscando na próxima casa a ser preenchida.
        new Timer(500, e -> {
            cursorVisivel = !cursorVisivel;
            repaint();
        }).start();
    }

    void mostrar(SessaoVotacao sessao, Modo modo) {
        this.sessao = sessao;
        this.modo = modo;
        cursorVisivel = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.translate(getInsets().left, getInsets().top);
        int largura = getWidth() - getInsets().left - getInsets().right;
        int altura = getHeight() - getInsets().top - getInsets().bottom;
        g2.setColor(TEXTO);

        switch (modo) {
            case FIM -> centralizado(g2, "FIM", new Font(Font.SANS_SERIF, Font.BOLD, 120), largura, altura / 2 + 40);
            case GRAVANDO -> desenharGravando(g2, largura, altura);
            case VOTACAO -> {
                if (sessao != null && !sessao.isFinalizada()) {
                    desenharVotacao(g2, largura, altura);
                }
            }
        }
        g2.dispose();
    }

    private void desenharGravando(Graphics2D g2, int largura, int altura) {
        centralizado(g2, "GRAVANDO...", FONTE_AVISO, largura, altura / 2 - 10);
        int barraLargura = largura / 2;
        int x = (largura - barraLargura) / 2;
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, altura / 2 + 15, barraLargura, 20);
        g2.fillRect(x + 3, altura / 2 + 18, (int) ((barraLargura - 5) * 0.7), 15);
    }

    private void desenharVotacao(Graphics2D g2, int largura, int altura) {
        Cargo cargo = sessao.getCargoAtual().orElseThrow();
        Optional<Voto> voto = sessao.getVotoEmPreparacao();
        int margem = 24;

        g2.setFont(FONTE_PEQUENA);
        g2.drawString("SEU VOTO PARA", margem, 34);
        centralizado(g2, cargo.getNome().toUpperCase(), FONTE_CARGO, largura - 150, 82);

        if (sessao.isBrancoSelecionado()) {
            centralizado(g2, "VOTO EM BRANCO", FONTE_AVISO, largura, altura / 2 + 10);
            desenharInstrucoes(g2, largura, altura);
            return;
        }

        // Casas do número
        int y = 130;
        g2.setFont(FONTE_MEDIA);
        g2.drawString("Número:", margem, y + 34);
        String digitos = sessao.getDigitos();
        int casaX = margem + 100;
        for (int i = 0; i < cargo.getQuantidadeDigitos(); i++) {
            int x = casaX + i * 48;
            g2.setStroke(new BasicStroke(2));
            g2.setColor(TEXTO);
            g2.drawRect(x, y, 40, 50);
            if (i < digitos.length()) {
                g2.setFont(FONTE_DIGITO);
                FontMetrics fm = g2.getFontMetrics();
                String d = String.valueOf(digitos.charAt(i));
                g2.drawString(d, x + (40 - fm.stringWidth(d)) / 2, y + 37);
            } else if (i == digitos.length() && cursorVisivel) {
                g2.fillRect(x + 6, y + 40, 28, 4);
            }
        }

        int linha = y + 95;
        Optional<Partido> partido = sessao.getPartidoDigitado();
        if (voto.isPresent() && voto.get() instanceof VotoNominal nominal) {
            g2.setFont(FONTE_MEDIA);
            g2.drawString("Nome: " + nominal.getCandidato().getNomeUrna(), margem, linha);
            linha += 32;
            g2.drawString("Partido: " + nominal.getCandidato().getPartido().getSigla()
                    + " - " + nominal.getCandidato().getPartido().getNome(), margem, linha);
            for (String extra : nominal.getCandidato().getInformacoesAdicionais()) {
                linha += 32;
                g2.drawString(extra, margem, linha);
            }
            desenharFoto(g2, largura - 150, 24, nominal.getCandidato().getNomeUrna(),
                    nominal.getCandidato().getPartido(), cargo.getNome());
        } else if (partido.isPresent() && (voto.isEmpty() || voto.get() instanceof VotoLegenda)) {
            // No proporcional a urna já mostra o partido ao digitar os 2 primeiros números.
            g2.setFont(FONTE_MEDIA);
            g2.drawString("Partido: " + partido.get().getSigla() + " - " + partido.get().getNome(), margem, linha);
        }

        if (voto.isPresent() && !voto.get().getMensagemTela().isEmpty()) {
            g2.setColor(DESTAQUE);
            centralizado(g2, voto.get().getMensagemTela(), FONTE_AVISO, largura, altura - 120);
            g2.setColor(TEXTO);
        }
        if (voto.isPresent()) {
            desenharInstrucoes(g2, largura, altura);
        }
    }

    private void desenharInstrucoes(Graphics2D g2, int largura, int altura) {
        int topo = altura - 82;
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(0, topo, largura, topo);
        g2.setFont(FONTE_PEQUENA);
        g2.drawString("Aperte a tecla:", 24, topo + 22);
        g2.drawString("CONFIRMA  para CONFIRMAR este voto", 60, topo + 46);
        g2.drawString("CORRIGE   para REINICIAR este voto", 60, topo + 68);
    }

    /** "Foto" do candidato: um avatar com as iniciais, na cor do partido fictício. */
    private void desenharFoto(Graphics2D g2, int x, int y, String nome, Partido partido, String cargo) {
        int largura = 120;
        int altura = 150;
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, largura, altura);
        g2.setColor(TEXTO);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(x, y, largura, altura);

        Color cor = Color.decode(partido.getCorHex());
        g2.setColor(cor);
        g2.fillOval(x + 25, y + 14, 70, 70);
        g2.fillRoundRect(x + 12, y + 92, 96, 50, 40, 40);

        g2.setColor(Color.WHITE);
        Font fonte = new Font(Font.SANS_SERIF, Font.BOLD, 28);
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        String iniciais = iniciais(nome);
        g2.drawString(iniciais, x + 60 - fm.stringWidth(iniciais) / 2, y + 59);

        g2.setColor(TEXTO);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        fm = g2.getFontMetrics();
        g2.drawString(cargo, x + 60 - fm.stringWidth(cargo) / 2, y + altura + 16);
    }

    private static String iniciais(String nome) {
        List<String> partes = List.of(nome.trim().split("\\s+"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, partes.size()); i++) {
            sb.append(Character.toUpperCase(partes.get(i).charAt(0)));
        }
        return sb.toString();
    }

    private static void centralizado(Graphics2D g2, String texto, Font fonte, int largura, int y) {
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (largura - fm.stringWidth(texto)) / 2, y);
    }
}
