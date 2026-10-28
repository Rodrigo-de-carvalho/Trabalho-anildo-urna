package br.com.urnacidada.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/** Teclado físico da urna: números, BRANCO, CORRIGE e CONFIRMA. */
class PainelTeclado extends JPanel {

    /** Quem recebe os apertos de tecla (a janela da urna). */
    interface Ouvinte {
        void numero(int digito);

        void branco();

        void corrige();

        void confirma();
    }

    private static final Color FUNDO = new Color(0x2B2B2B);
    private static final Color TECLA = new Color(0x111111);

    PainelTeclado(Ouvinte ouvinte) {
        super(new BorderLayout(0, 14));
        setBackground(FUNDO);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 22, 22));

        JLabel titulo = new JLabel("JUSTIÇA ELEITORAL • SIMULAÇÃO", SwingConstants.CENTER);
        titulo.setForeground(new Color(0xBDBDBD));
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 13f));
        add(titulo, BorderLayout.NORTH);

        JPanel numeros = new JPanel(new GridLayout(4, 3, 12, 12));
        numeros.setOpaque(false);
        for (int i = 1; i <= 9; i++) {
            numeros.add(teclaNumerica(i, ouvinte));
        }
        numeros.add(espaco());
        numeros.add(teclaNumerica(0, ouvinte));
        numeros.add(espaco());
        add(numeros, BorderLayout.CENTER);

        JPanel acoes = new JPanel(new GridLayout(1, 3, 10, 0));
        acoes.setOpaque(false);
        acoes.add(teclaAcao("BRANCO", Color.WHITE, Color.BLACK, ouvinte::branco));
        acoes.add(teclaAcao("CORRIGE", new Color(0xF57C00), Color.BLACK, ouvinte::corrige));
        acoes.add(teclaAcao("CONFIRMA", new Color(0x2E7D32), Color.BLACK, ouvinte::confirma));
        add(acoes, BorderLayout.SOUTH);
    }

    private static JButton teclaNumerica(int digito, Ouvinte ouvinte) {
        JButton b = base(String.valueOf(digito), TECLA, Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        b.setPreferredSize(new Dimension(76, 56));
        b.addActionListener(e -> ouvinte.numero(digito));
        return b;
    }

    private static JButton teclaAcao(String texto, Color fundo, Color letra, Runnable acao) {
        JButton b = base(texto, fundo, letra);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setPreferredSize(new Dimension(100, 62));
        b.addActionListener(e -> acao.run());
        return b;
    }

    private static JButton base(String texto, Color fundo, Color letra) {
        JButton b = new JButton(texto);
        b.setBackground(fundo);
        b.setForeground(letra);
        b.setFocusable(false); // o teclado do computador continua funcionando na janela
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JPanel espaco() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }
}
