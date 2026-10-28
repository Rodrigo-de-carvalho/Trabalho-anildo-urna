package br.com.urnacidada.ui;

import br.com.urnacidada.modelo.Partido;
import br.com.urnacidada.servico.Apuracao;
import br.com.urnacidada.servico.ResultadoCargo;
import br.com.urnacidada.servico.ServicoUrna;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Boletim de urna: resultado de cada cargo, lido do banco de dados. */
class DialogoApuracao extends JDialog {

    private static final String[] COLUNAS = {"Número", "Nome", "Partido", "Votos", "% dos válidos"};

    private final ServicoUrna servico;
    private final JTabbedPane abas = new JTabbedPane();
    private final JLabel resumo = new JLabel("Carregando...");
    private String boletimTexto = "";

    DialogoApuracao(JFrame dono, ServicoUrna servico) {
        super(dono, "Apuração - Boletim de Urna", true);
        this.servico = servico;

        resumo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        resumo.setFont(resumo.getFont().deriveFont(Font.BOLD, 14f));

        JButton atualizar = new JButton("Atualizar");
        atualizar.addActionListener(e -> carregar());
        JButton copiar = new JButton("Copiar boletim");
        copiar.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(boletimTexto), null));
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(e -> dispose());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(atualizar);
        botoes.add(copiar);
        botoes.add(fechar);

        add(resumo, BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        setSize(new Dimension(760, 560));
        setLocationRelativeTo(dono);
        carregar();
    }

    private void carregar() {
        resumo.setText("Carregando votos de " + servico.getRepositorio().getDescricao() + "...");
        new SwingWorker<Apuracao, Void>() {
            @Override
            protected Apuracao doInBackground() throws Exception {
                return servico.apurar();
            }

            @Override
            protected void done() {
                try {
                    mostrar(get());
                } catch (InterruptedException | ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    resumo.setText("Erro ao apurar: " + causa.getMessage());
                }
            }
        }.execute();
    }

    private void mostrar(Apuracao apuracao) {
        abas.removeAll();
        StringBuilder boletim = new StringBuilder("BOLETIM DE URNA - ")
                .append(servico.getEleicao().getTitulo()).append("\n")
                .append("Comparecimento: ").append(apuracao.getComparecimento()).append(" eleitor(es)\n\n");

        for (ResultadoCargo r : apuracao.getResultados()) {
            abas.addTab(r.getCargo().getNome(), painelCargo(r));
            boletim.append(textoCargo(r)).append('\n');
        }
        String texto = "Comparecimento: " + apuracao.getComparecimento() + " eleitor(es)";
        if (apuracao.getRegistrosIgnorados() > 0) {
            texto += "   (" + apuracao.getRegistrosIgnorados() + " registro(s) fora da configuração atual ignorado(s))";
        }
        resumo.setText(texto);
        boletimTexto = boletim.toString();
    }

    private JPanel painelCargo(ResultadoCargo r) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        DefaultTableModel modelo = modeloSomenteLeitura(COLUNAS);
        for (ResultadoCargo.Linha l : r.getLinhasCandidatos()) {
            modelo.addRow(new Object[]{l.numero(), l.nome(), l.partido(), l.votos(), r.percentualValidos(l.votos())});
        }
        for (ResultadoCargo.Linha l : r.getLinhasLegenda()) {
            modelo.addRow(new Object[]{l.numero(), "Legenda: " + l.nome(), l.partido(), l.votos(),
                    r.percentualValidos(l.votos())});
        }
        painel.add(new JLabel("Sistema " + r.getCargo().getSistemaEleitoral()));
        painel.add(new JScrollPane(tabela(modelo)));

        if (r.getCargo().permiteVotoLegenda()) {
            DefaultTableModel partidos = modeloSomenteLeitura(new String[]{"Número", "Partido", "Sigla", "Votos", "% dos válidos"});
            for (Map.Entry<Partido, Long> e : r.getVotosPorPartido().entrySet()) {
                partidos.addRow(new Object[]{e.getKey().getNumeroFormatado(), e.getKey().getNome(),
                        e.getKey().getSigla(), e.getValue(), r.percentualValidos(e.getValue())});
            }
            painel.add(new JLabel("Votos por partido (nominais + legenda) - definem as cadeiras no sistema proporcional"));
            painel.add(new JScrollPane(tabela(partidos)));
        }

        JLabel totais = new JLabel(String.format(Locale.ROOT,
                "Válidos: %d    Brancos: %d    Nulos: %d    Total: %d",
                r.getVotosValidos(), r.getBrancos(), r.getNulos(), r.getTotal()));
        totais.setFont(totais.getFont().deriveFont(Font.BOLD));
        totais.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        painel.add(totais);
        return painel;
    }

    private static JTable tabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(24);
        tabela.setAutoCreateRowSorter(true);
        tabela.getColumnModel().getColumn(4).setCellRenderer(new RenderizadorPercentual());
        tabela.getColumnModel().getColumn(0).setMaxWidth(80);
        tabela.getColumnModel().getColumn(3).setMaxWidth(80);
        return tabela;
    }

    private static DefaultTableModel modeloSomenteLeitura(String[] colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int coluna) {
                return switch (coluna) {
                    case 3 -> Long.class;
                    case 4 -> Double.class;
                    default -> String.class;
                };
            }
        };
    }

    private static String textoCargo(ResultadoCargo r) {
        StringBuilder sb = new StringBuilder(r.getCargo().getNome().toUpperCase()).append('\n');
        List<ResultadoCargo.Linha> linhas = r.getLinhasCandidatos();
        linhas.forEach(l -> sb.append(String.format(Locale.ROOT, "  %-6s %-22s %-5s %5d%n",
                l.numero(), l.nome(), l.partido(), l.votos())));
        r.getLinhasLegenda().forEach(l -> sb.append(String.format(Locale.ROOT, "  %-6s %-22s %-5s %5d%n",
                l.numero(), "Legenda", l.partido(), l.votos())));
        sb.append(String.format(Locale.ROOT, "  Válidos: %d | Brancos: %d | Nulos: %d | Total: %d%n",
                r.getVotosValidos(), r.getBrancos(), r.getNulos(), r.getTotal()));
        return sb.toString();
    }

    /** Mostra o percentual como uma barra, facilitando a leitura na oficina. */
    private static final class RenderizadorPercentual extends JProgressBar implements TableCellRenderer {
        RenderizadorPercentual() {
            super(0, 1000);
            setStringPainted(true);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionado,
                                                                boolean foco, int linha, int coluna) {
            double percentual = valor instanceof Double d ? d : 0.0;
            setValue((int) Math.round(percentual * 10));
            setString(String.format("%.1f%%", percentual));
            return this;
        }
    }
}
