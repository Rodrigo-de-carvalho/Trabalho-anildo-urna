package br.com.urnacidada.ui;

import br.com.urnacidada.persistencia.PersistenciaException;
import br.com.urnacidada.servico.ServicoUrna;
import br.com.urnacidada.servico.SessaoVotacao;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;

/** Janela principal: tela + teclado da urna, menus do mesário e barra de status. */
public class JanelaUrna extends JFrame implements PainelTeclado.Ouvinte {

    private static final int TEMPO_TELA_FIM_MS = 3500;

    private final ServicoUrna servico;
    private final String senhaMesario;
    private final PainelTela tela = new PainelTela();
    private final JLabel status = new JLabel();
    private SessaoVotacao sessao;
    private PainelTela.Modo modo = PainelTela.Modo.VOTACAO;
    private int eleitoresNestaExecucao;

    public JanelaUrna(ServicoUrna servico, String senhaMesario) {
        super("Urna Cidadã - " + servico.getEleicao().getTitulo());
        this.servico = servico;
        this.senhaMesario = senhaMesario;

        JPanel corpo = new JPanel(new BorderLayout(18, 0));
        corpo.setBackground(new Color(0xD9D4C7));
        corpo.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        corpo.add(tela, BorderLayout.CENTER);
        corpo.add(new PainelTeclado(this), BorderLayout.EAST);

        status.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        add(corpo, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setJMenuBar(criarMenu());
        configurarTecladoDoComputador();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        novoEleitor();
    }

    /** Mostra um aviso na barra de status (pode ser chamado de qualquer thread). */
    public void avisar(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            status.setForeground(new Color(0xB71C1C));
            status.setText("⚠ " + mensagem);
        });
    }

    private void novoEleitor() {
        sessao = servico.novaSessao();
        modo = PainelTela.Modo.VOTACAO;
        tela.mostrar(sessao, modo);
        atualizarStatus();
    }

    private void atualizarStatus() {
        status.setForeground(Color.DARK_GRAY);
        status.setText("Armazenamento: " + servico.getRepositorio().getDescricao()
                + "   |   Eleitores nesta execução: " + eleitoresNestaExecucao
                + "   |   Teclado: 0-9, Enter = CONFIRMA, Backspace = CORRIGE, B = BRANCO");
    }

    private boolean votando() {
        return modo == PainelTela.Modo.VOTACAO;
    }

    // ---- Teclas da urna ----------------------------------------------------------------

    @Override
    public void numero(int digito) {
        if (votando() && sessao.digitar(digito)) {
            Som.tecla();
            tela.repaint();
        }
    }

    @Override
    public void branco() {
        if (!votando()) {
            return;
        }
        if (sessao.votarEmBranco()) {
            Som.tecla();
            tela.repaint();
        } else {
            Som.erro(); // para votar em branco é preciso apertar CORRIGE antes
        }
    }

    @Override
    public void corrige() {
        if (votando()) {
            Som.tecla();
            sessao.corrigir();
            tela.repaint();
        }
    }

    @Override
    public void confirma() {
        if (!votando()) {
            return;
        }
        if (sessao.confirmar().isEmpty()) {
            Som.erro();
            return;
        }
        if (sessao.isFinalizada()) {
            gravarVotos();
        } else {
            Som.confirma();
            tela.repaint();
        }
    }

    /** Grava fora da thread da interface para a tela não travar enquanto fala com a nuvem. */
    private void gravarVotos() {
        modo = PainelTela.Modo.GRAVANDO;
        tela.mostrar(sessao, modo);
        SessaoVotacao concluida = sessao;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                servico.registrar(concluida);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    eleitoresNestaExecucao++;
                    modo = PainelTela.Modo.FIM;
                    tela.mostrar(sessao, modo);
                    atualizarStatus();
                    Som.fim();
                    Timer proximo = new Timer(TEMPO_TELA_FIM_MS, e -> novoEleitor());
                    proximo.setRepeats(false);
                    proximo.start();
                } catch (InterruptedException | ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(JanelaUrna.this,
                            "Não foi possível gravar o voto:\n" + causa.getMessage()
                                    + "\n\nO eleitor deve votar novamente.",
                            "Erro ao gravar", JOptionPane.ERROR_MESSAGE);
                    novoEleitor();
                }
            }
        }.execute();
    }

    // ---- Teclado do computador -------------------------------------------------------

    private void configurarTecladoDoComputador() {
        JComponent raiz = getRootPane();
        for (int d = 0; d <= 9; d++) {
            int digito = d;
            atalho(raiz, "num" + d, () -> numero(digito),
                    KeyStroke.getKeyStroke(KeyEvent.VK_0 + d, 0),
                    KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + d, 0));
        }
        atalho(raiz, "confirma", this::confirma, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        atalho(raiz, "corrige", this::corrige, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        atalho(raiz, "branco", this::branco, KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));
    }

    private static void atalho(JComponent raiz, String nome, Runnable acao, KeyStroke... teclas) {
        for (KeyStroke tecla : teclas) {
            raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tecla, nome);
        }
        raiz.getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
            }
        });
    }

    // ---- Menus -----------------------------------------------------------------------

    private JMenuBar criarMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu mesario = new JMenu("Mesário");
        JMenuItem apuracao = new JMenuItem("Apuração / Boletim de Urna...");
        apuracao.addActionListener(e -> {
            if (pedirSenha()) {
                new DialogoApuracao(this, servico).setVisible(true);
            }
        });
        JMenuItem zerar = new JMenuItem("Zerar urna (nova oficina)...");
        zerar.addActionListener(e -> zerarUrna());
        JCheckBoxMenuItem som = new JCheckBoxMenuItem("Som ligado", Som.isAtivo());
        som.addActionListener(e -> Som.setAtivo(som.isSelected()));
        mesario.add(apuracao);
        mesario.add(zerar);
        mesario.addSeparator();
        mesario.add(som);

        JMenu ajuda = new JMenu("Ajuda");
        JMenuItem comoVotar = new JMenuItem("Como votar");
        comoVotar.addActionListener(e -> JOptionPane.showMessageDialog(this, TextosAjuda.COMO_VOTAR,
                "Como votar", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem candidatos = new JMenuItem("Candidatos fictícios");
        candidatos.addActionListener(e -> JOptionPane.showMessageDialog(this,
                TextosAjuda.listaCandidatos(servico.getEleicao()), "Candidatos fictícios",
                JOptionPane.INFORMATION_MESSAGE));
        JMenuItem sobre = new JMenuItem("Sobre a Urna Cidadã");
        sobre.addActionListener(e -> JOptionPane.showMessageDialog(this, TextosAjuda.SOBRE,
                "Sobre", JOptionPane.INFORMATION_MESSAGE));
        ajuda.add(comoVotar);
        ajuda.add(candidatos);
        ajuda.add(sobre);

        barra.add(mesario);
        barra.add(ajuda);
        return barra;
    }

    private boolean pedirSenha() {
        JPasswordField campo = new JPasswordField(10);
        int opcao = JOptionPane.showConfirmDialog(this, new Object[]{"Senha do mesário:", campo},
                "Acesso restrito", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) {
            return false;
        }
        if (!senhaMesario.equals(new String(campo.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Senha incorreta.", "Acesso negado", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void zerarUrna() {
        if (!pedirSenha()) {
            return;
        }
        int opcao = JOptionPane.showConfirmDialog(this,
                "Isso apaga TODOS os votos gravados em " + servico.getRepositorio().getDescricao()
                        + ".\nDeseja continuar?", "Zerar urna", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws PersistenciaException {
                servico.zerarUrna();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    eleitoresNestaExecucao = 0;
                    novoEleitor();
                    JOptionPane.showMessageDialog(JanelaUrna.this, "Urna zerada. Pronta para a próxima oficina!");
                } catch (InterruptedException | ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(JanelaUrna.this, "Falha ao zerar: " + causa.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
