import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PageReplacementGUI {

    private JFrame frame;
    private JFormattedTextField tfNumPages;
    private JFormattedTextField tfMaxValue;
    private JFormattedTextField tfNumFrames;
    private JButton btnRun;

    private JCheckBox cbToggleInput;
    private JTextArea taManualInput;
    private JPanel pnlAleatorioInputs;
    private JScrollPane spManualInput;

    private JTextArea taFIFO, taLRU, taOPT, taCLOCK;

    private final AtomicInteger remaining = new AtomicInteger(0);
    private File saveFile;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PageReplacementGUI().createAndShowGui());
    }

    private void createAndShowGui() {
        frame = new JFrame("Simulador de Substituição de Páginas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(950, 750);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(6, 6));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        NumberFormat intFormat = NumberFormat.getIntegerInstance();
        intFormat.setGroupingUsed(false);

        cbToggleInput = new JCheckBox("Usar Entrada Manual de Páginas (Separar por vírgulas ou espaços)");
        cbToggleInput.setToolTipText("Marque para digitar sua própria sequência de páginas.");

        c.gridx = 0; c.gridy = 0; c.gridwidth = 3; c.anchor = GridBagConstraints.CENTER;
        form.add(cbToggleInput, c);

        c.gridwidth = 1; c.anchor = GridBagConstraints.WEST;

        pnlAleatorioInputs = new JPanel(new GridBagLayout());
        GridBagConstraints cA = new GridBagConstraints();
        cA.insets = new Insets(2, 2, 2, 2);
        cA.anchor = GridBagConstraints.WEST;

        cA.gridx = 0; cA.gridy = 0;
        pnlAleatorioInputs.add(new JLabel("Número total de páginas (1 a 100000):"), cA);
        cA.gridx = 1;
        tfNumPages = new JFormattedTextField(intFormat);
        tfNumPages.setColumns(10);
        tfNumPages.setText("1000");
        pnlAleatorioInputs.add(tfNumPages, cA);

        // Max Value
        cA.gridx = 0; cA.gridy = 1;
        pnlAleatorioInputs.add(new JLabel("Valor máximo da página (1 a 50):"), cA);
        cA.gridx = 1;
        tfMaxValue = new JFormattedTextField(intFormat);
        tfMaxValue.setColumns(10);
        tfMaxValue.setText("20");
        pnlAleatorioInputs.add(tfMaxValue, cA);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(pnlAleatorioInputs, c);

        taManualInput = new JTextArea(4, 30);
        taManualInput.setText("1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5"); // Exemplo de entrada
        taManualInput.setLineWrap(true);
        taManualInput.setToolTipText("Insira números inteiros separados por vírgulas ou espaços.");
        spManualInput = new JScrollPane(taManualInput);

        JPanel pnlManual = new JPanel(new BorderLayout());
        pnlManual.setBorder(BorderFactory.createTitledBorder("Sequência de Páginas Manual"));
        pnlManual.add(spManualInput, BorderLayout.CENTER);

        c.gridx = 2; c.gridy = 1; c.gridwidth = 1; c.fill = GridBagConstraints.BOTH;
        form.add(pnlManual, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 1; c.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Número de frames (1 a 10):"), c);
        c.gridx = 1;
        tfNumFrames = new JFormattedTextField(intFormat);
        tfNumFrames.setColumns(10);
        tfNumFrames.setText("4");
        form.add(tfNumFrames, c);

        c.gridx = 2; c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        btnRun = new JButton("Gerar e Rodar");
        btnRun.addActionListener(this::onRun);
        form.add(btnRun, c);

        cbToggleInput.addActionListener(this::toggleInputMode);
        toggleInputMode(null);

        frame.add(form, BorderLayout.NORTH);

        JPanel logPanelContainer = new JPanel(new GridLayout(2, 2, 6, 6));
        logPanelContainer.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));

        taFIFO = createLogTextArea();
        taLRU = createLogTextArea();
        taOPT = createLogTextArea();
        taCLOCK = createLogTextArea();

        logPanelContainer.add(createLogPanel("FIFO", taFIFO));
        logPanelContainer.add(createLogPanel("LRU", taLRU));
        logPanelContainer.add(createLogPanel("Ótimo", taOPT));
        logPanelContainer.add(createLogPanel("Relógio", taCLOCK));

        frame.add(logPanelContainer, BorderLayout.CENTER);


        frame.setVisible(true);
    }

    private void toggleInputMode(ActionEvent ev) {
        boolean manualEnabled = cbToggleInput.isSelected();

        tfNumPages.setEnabled(!manualEnabled);
        tfMaxValue.setEnabled(!manualEnabled);
        pnlAleatorioInputs.setEnabled(!manualEnabled);

        taManualInput.setEnabled(manualEnabled);
        spManualInput.getViewport().setBackground(manualEnabled ? UIManager.getColor("Text.background") : UIManager.getColor("control"));

        btnRun.setText(manualEnabled ? "Rodar" : "Gerar e Rodar");
    }

    private void onRun(ActionEvent ev) {
        btnRun.setEnabled(false);

        int numFrames;
        int[] pages;

        try {
            numFrames = Integer.parseInt(tfNumFrames.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Insira um número inteiro válido para Frames.", "Entrada inválida", JOptionPane.ERROR_MESSAGE);
            btnRun.setEnabled(true);
            return;
        }
        if (numFrames < 1 || numFrames > 10) {
            JOptionPane.showMessageDialog(frame, "Número de frames deve estar entre 1 e 10.", "Entrada inválida", JOptionPane.ERROR_MESSAGE);
            btnRun.setEnabled(true);
            return;
        }


        if (cbToggleInput.isSelected()) {
            try {
                pages = parsePages(taManualInput.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Entrada Manual inválida: " + ex.getMessage(), "Entrada inválida", JOptionPane.ERROR_MESSAGE);
                btnRun.setEnabled(true);
                return;
            }
        } else {
            int numPages, maxValue;
            try {
                numPages = Integer.parseInt(tfNumPages.getText().trim());
                maxValue = Integer.parseInt(tfMaxValue.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Insira números inteiros válidos para Geração Aleatória.", "Entrada inválida", JOptionPane.ERROR_MESSAGE);
                btnRun.setEnabled(true);
                return;
            }

            if (numPages < 1 || numPages > 100000) {
                JOptionPane.showMessageDialog(frame, "Número de páginas para Geração deve estar entre 1 e 100000.", "Entrada inválida", JOptionPane.ERROR_MESSAGE);
                btnRun.setEnabled(true);
                return;
            }
            if (maxValue < 1 || maxValue > 50) {
                JOptionPane.showMessageDialog(frame, "Valor máximo da página deve estar entre 1 e 50.", "Entrada inválida", JOptionPane.ERROR_MESSAGE);
                btnRun.setEnabled(true);
                return;
            }

            pages = generatePages(numPages, maxValue);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultName = "log_" + sdf.format(new Date()) + ".txt";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar log como...");
        chooser.setSelectedFile(new File(defaultName));
        int res = chooser.showSaveDialog(frame);
        if (res != JFileChooser.APPROVE_OPTION) {
            btnRun.setEnabled(true);
            return;
        }
        saveFile = chooser.getSelectedFile();

        String pageSequence = "Sequência de Páginas (" + pages.length + " páginas):\n"
                + Arrays.toString(pages)
                + "\n\n";

        taFIFO.setText(pageSequence);
        taLRU.setText(pageSequence);
        taOPT.setText(pageSequence);
        taCLOCK.setText(pageSequence);


        remaining.set(4);
        AlgorithmWorker wFIFO = new AlgorithmWorker("FIFO", pages, numFrames, taFIFO);
        AlgorithmWorker wLRU  = new AlgorithmWorker("LRU", pages, numFrames, taLRU);
        AlgorithmWorker wOPT  = new AlgorithmWorker("Ótimo", pages, numFrames, taOPT);
        AlgorithmWorker wCLOCK= new AlgorithmWorker("Relógio", pages, numFrames, taCLOCK);

        wFIFO.execute();
        wLRU.execute();
        wOPT.execute();
        wCLOCK.execute();
    }

    private int[] parsePages(String input) throws NumberFormatException {
        String[] tokens = input.trim().replaceAll("\\s+", ",").split(",");

        if (tokens.length == 0 || (tokens.length == 1 && tokens[0].isEmpty())) {
            throw new NumberFormatException("A sequência de páginas não pode ser vazia.");
        }

        List<Integer> pagesList = new ArrayList<>();
        for (String token : tokens) {
            token = token.trim();
            if (!token.isEmpty()) {
                try {
                    int page = Integer.parseInt(token);
                    if (page < 0) {
                        throw new NumberFormatException("Página deve ser um valor não-negativo.");
                    }
                    pagesList.add(page);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Valor inválido na sequência: '" + token + "'");
                }
            }
        }

        if (pagesList.isEmpty()) {
            throw new NumberFormatException("Nenhuma página válida encontrada.");
        }

        return pagesList.stream().mapToInt(i -> i).toArray();
    }

    private JTextArea createLogTextArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return ta;
    }

    private JScrollPane createLogPanel(String title, JTextArea ta) {
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, TitledBorder.LEFT, TitledBorder.TOP));
        return sp;
    }

    private int[] generatePages(int numPages, int maxValue) {
        Random r = new Random();
        int[] pages = new int[numPages];
        for (int i = 0; i < numPages; i++) {
            pages[i] = r.nextInt(maxValue + 1);
        }
        return pages;
    }

    private class AlgorithmWorker extends SwingWorker<Void, String> {
        private final String name;
        private final int[] pages;
        private final int numFrames;
        private final JTextArea targetTextArea;
        private final StringBuilder logBuilder = new StringBuilder();

        AlgorithmWorker(String name, int[] pages, int numFrames, JTextArea targetTextArea) {
            this.name = name;
            this.pages = pages;
            this.numFrames = numFrames;
            this.targetTextArea = targetTextArea;
            // Escreve a linha inicial diretamente na JTextArea
            this.targetTextArea.append(String.format("Iniciando Algoritmo %s...\n", name));
        }

        String getNameAlg() { return name; }
        String getFullLog() { return logBuilder.toString(); }

        @Override
        protected Void doInBackground() throws Exception {
            Consumer<String> logger = this::publish;

            switch (name) {
                case "FIFO":
                    try { PageReplacement.fifo(pages, numFrames, logger); } catch (Throwable t) { publish("Erro: " + t.getMessage()); }
                    break;
                case "LRU":
                    try { PageReplacement.lru(pages, numFrames, logger); } catch (Throwable t) { publish("Erro: " + t.getMessage()); }
                    break;
                case "Ótimo":
                    try { PageReplacement.optimal(pages, numFrames, logger); } catch (Throwable t) { publish("Erro: " + t.getMessage()); }
                    break;
                case "Relógio":
                    try { PageReplacement.clock(pages, numFrames, logger); } catch (Throwable t) { publish("Erro: " + t.getMessage()); }
                    break;
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String s : chunks) {
                targetTextArea.append(s + "\n");
                targetTextArea.setCaretPosition(targetTextArea.getDocument().getLength());
                logBuilder.append(s).append(System.lineSeparator());
            }
        }

        @Override
        protected void done() {
            targetTextArea.append(String.format("Fim do Algoritmo %s\n", name));
            targetTextArea.setCaretPosition(targetTextArea.getDocument().getLength());

            int left = remaining.decrementAndGet();
            if (left == 0) {
                saveAllLogs();
            }
        }
    }

    private void saveAllLogs() {
        StringBuilder sb = new StringBuilder();
        sb.append("Page Replacement - Log\n");
        sb.append("Gerado em: ").append(new Date()).append("\n\n");

        sb.append("------- FIFO -------\n").append(taFIFO.getText()).append("\n\n");
        sb.append("------- LRU -------\n").append(taLRU.getText()).append("\n\n");
        sb.append("------- OTIMO -------\n").append(taOPT.getText()).append("\n\n");
        sb.append("------- RELOGIO -------\n").append(taCLOCK.getText()).append("\n\n");

        try (FileWriter fw = new FileWriter(saveFile)) {
            fw.write(sb.toString());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "Logs salvos em:\n" + saveFile.getAbsolutePath(), "Concluído", JOptionPane.INFORMATION_MESSAGE);
                btnRun.setEnabled(true);
            });
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "Erro ao salvar logs: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                btnRun.setEnabled(true);
            });
        }
    }
}