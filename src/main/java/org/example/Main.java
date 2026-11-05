package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import java.util.Random;

public class Main extends JFrame {

    public static final String[] CLASS_NAMES = {
            "1-й клас (→)",
            "2-й клас (↵)",
            "3-й клас (←)",
            "4-й клас (↓)"
    };

    public static final int[][] IDEAL_PATTERNS = {
            {
                    0,0,0,0,0,1,1,0,0,0,
                    0,0,0,0,0,1,1,1,0,0,
                    0,0,0,0,0,1,1,1,1,0,
                    1,1,1,1,1,1,1,1,1,0,
                    1,1,1,1,1,1,1,1,1,1,
                    1,1,1,1,1,1,1,1,1,1,
                    1,1,1,1,1,1,1,1,1,0,
                    0,0,0,0,0,1,1,1,1,0,
                    0,0,0,0,0,1,1,1,0,0,
                    0,0,0,0,0,1,1,0,0,0
            },
            {
                    0,0,0,0,0,0,0,1,1,1,
                    0,0,0,0,0,0,0,1,1,1,
                    0,0,0,0,0,0,0,1,1,1,
                    0,0,0,1,0,0,0,1,1,1,
                    0,0,1,1,0,0,0,1,1,1,
                    0,1,1,1,0,0,0,1,1,1,
                    1,1,1,1,1,1,1,1,1,1,
                    1,1,1,1,1,1,1,1,1,1,
                    0,1,1,1,1,1,1,1,1,0,
                    0,0,1,1,0,0,0,0,0,0
            },
            {
                    0,0,0,1,1,0,0,0,0,0,
                    0,0,1,1,1,0,0,0,0,0,
                    0,1,1,1,1,1,1,1,1,1,
                    1,1,1,1,1,1,1,1,1,1,
                    1,1,1,0,0,0,0,0,1,1,
                    1,1,1,0,0,0,0,0,1,1,
                    1,1,1,1,1,1,1,1,1,1,
                    0,1,1,1,1,1,1,1,1,1,
                    0,0,1,1,1,0,0,0,0,0,
                    0,0,0,1,1,0,0,0,0,0
            },
            {
                    0,0,1,1,1,1,1,1,0,0,
                    0,0,1,1,1,1,1,1,0,0,
                    0,0,0,1,1,1,1,0,0,0,
                    0,0,0,0,1,1,0,0,0,0,
                    0,0,0,0,1,1,0,0,0,0,
                    1,1,0,0,1,1,0,0,1,1,
                    1,1,1,0,1,1,0,1,1,1,
                    0,1,1,1,1,1,1,1,1,0,
                    0,0,1,1,1,1,1,1,0,0,
                    0,0,0,1,1,1,1,0,0,0
            }
    };

    public static final int[] NOISE_LEVELS = {10, 20, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100};
    public static final int SAMPLES_PER_NOISE = 10;

    private final HopfieldNetwork network;
    private final int[] currentInput = new int[100];
    private final int[] currentOutput = new int[100];
    private final GridPanel inputGrid;
    private final GridPanel outputGrid;
    private final JTextField[] similarityFields = new JTextField[4];
    private final JComboBox<String> classCombo;
    private final JComboBox<String> noiseCombo;
    private final JComboBox<String> sampleCombo;
    private final JLabel capacityLabel;
    private final JLabel statsLabel;
    private final JLabel resultLabel;

    public Main() {
        super("Лабораторна робота №5 — Нейронна мережа Хопфілда (Варіант 7)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        network = new HopfieldNetwork(100);
        network.train(IDEAL_PATTERNS);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Вхідний образ (X)",
                TitledBorder.CENTER, TitledBorder.TOP));
        inputGrid = new GridPanel(currentInput, 18, true);
        inputWrapper.add(inputGrid, BorderLayout.CENTER);

        JPanel outputWrapper = new JPanel(new BorderLayout());
        outputWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Відновлений образ (Y)",
                TitledBorder.CENTER, TitledBorder.TOP));
        outputGrid = new GridPanel(currentOutput, 18, false);
        outputWrapper.add(outputGrid, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(7, 1, 5, 5));
        controlPanel.setPreferredSize(new Dimension(405, 198));
        controlPanel.setMinimumSize(new Dimension(405, 198));
        controlPanel.setMaximumSize(new Dimension(405, 198));

        classCombo = new JComboBox<>(CLASS_NAMES);

        String[] noiseNames = new String[NOISE_LEVELS.length];
        for (int i = 0; i < NOISE_LEVELS.length; i++) {
            noiseNames[i] = "Зашумлення образу: " + NOISE_LEVELS[i] + "%";
        }
        noiseCombo = new JComboBox<>(noiseNames);

        String[] sampleNames = new String[SAMPLES_PER_NOISE];
        for (int i = 0; i < SAMPLES_PER_NOISE; i++) {
            sampleNames[i] = "Тестовий зразок №" + (i + 1) + " (із 10)";
        }
        sampleCombo = new JComboBox<>(sampleNames);

        JButton restoreBtn = new JButton("Подати образ та відновити");
        restoreBtn.addActionListener(e -> loadSelectedSample());

        capacityLabel = new JLabel(String.format(Locale.US,
                "Ємність мережі (n = 100): m_max = %.2f (збережено 4 еталони)",
                network.getMaxCapacity()));
        capacityLabel.setFont(capacityLabel.getFont().deriveFont(Font.PLAIN, 12f));

        statsLabel = new JLabel("Статистика серії (10 зразків): —");
        statsLabel.setFont(statsLabel.getFont().deriveFont(Font.BOLD, 12f));

        resultLabel = new JLabel("Розпізнано: —");
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 12.5f));

        controlPanel.add(classCombo);
        controlPanel.add(noiseCombo);
        controlPanel.add(sampleCombo);
        controlPanel.add(restoreBtn);
        controlPanel.add(capacityLabel);
        controlPanel.add(statsLabel);
        controlPanel.add(resultLabel);

        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(inputWrapper);
        topPanel.add(Box.createHorizontalStrut(16));
        topPanel.add(outputWrapper);
        topPanel.add(Box.createHorizontalStrut(18));
        topPanel.add(controlPanel);
        topPanel.add(Box.createHorizontalGlue());

        JPanel centerPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        for (int c = 0; c < 4; c++) {
            JPanel box = new JPanel(new BorderLayout());
            box.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Еталон: " + CLASS_NAMES[c],
                    TitledBorder.CENTER, TitledBorder.TOP));
            GridPanel idealGrid = new GridPanel(IDEAL_PATTERNS[c], 15, false);
            box.add(idealGrid, BorderLayout.CENTER);
            centerPanel.add(box);
        }

        JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Збіг відновленого образу Y з еталонами (%)",
                TitledBorder.LEFT, TitledBorder.TOP));
        for (int c = 0; c < 4; c++) {
            JTextField tf = new JTextField();
            tf.setEditable(false);
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setFont(new Font("Monospaced", Font.BOLD, 14));
            similarityFields[c] = tf;
            bottomPanel.add(tf);
        }

        root.add(topPanel, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        classCombo.addActionListener(e -> loadSelectedSample());
        noiseCombo.addActionListener(e -> loadSelectedSample());
        sampleCombo.addActionListener(e -> loadSelectedSample());
        loadSelectedSample();
    }

    public void selectSample(int classIdx, int noiseIdx, int sampleIdx) {
        classCombo.setSelectedIndex(classIdx);
        noiseCombo.setSelectedIndex(noiseIdx);
        sampleCombo.setSelectedIndex(sampleIdx);
        loadSelectedSample();
    }

    private void loadSelectedSample() {
        int c = classCombo.getSelectedIndex();
        int nIdx = noiseCombo.getSelectedIndex();
        int sIdx = sampleCombo.getSelectedIndex();
        int noisePct = NOISE_LEVELS[nIdx];

        long seed = 1000L * (c + 1) + 100L * noisePct + sIdx;
        int[] noisy = makeNoisyPattern(IDEAL_PATTERNS[c], noisePct, seed);
        System.arraycopy(noisy, 0, currentInput, 0, 100);
        inputGrid.repaint();
        recognizeCurrent();
    }

    public static int[] makeNoisyPattern(int[] ideal, int noisePixels, long seed) {
        int[] res = ideal.clone();
        boolean[] flipped = new boolean[100];
        Random rnd = new Random(seed);
        int count = 0;
        while (count < noisePixels) {
            int idx = rnd.nextInt(100);
            if (!flipped[idx]) {
                flipped[idx] = true;
                res[idx] = 1 - res[idx];
                count++;
            }
        }
        return res;
    }

    public static double computeMatchPercent(int[] a, int[] b) {
        int same = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == b[i]) {
                same++;
            }
        }
        return same * 100.0 / a.length;
    }

    private void recognizeCurrent() {
        int[] restored = network.recognize(currentInput, 50);
        System.arraycopy(restored, 0, currentOutput, 0, 100);
        outputGrid.repaint();

        int c = classCombo.getSelectedIndex();
        int noisePct = NOISE_LEVELS[noiseCombo.getSelectedIndex()];

        int exactSeriesCount = 0;
        for (int s = 0; s < SAMPLES_PER_NOISE; s++) {
            long seed = 1000L * (c + 1) + 100L * noisePct + s;
            int[] testIn = makeNoisyPattern(IDEAL_PATTERNS[c], noisePct, seed);
            int[] testOut = network.recognize(testIn, 50);
            double m = computeMatchPercent(testOut, IDEAL_PATTERNS[c]);
            if (m == 100.0) {
                exactSeriesCount++;
            }
        }

        network.recognize(currentInput, 50);

        double[] matches = new double[4];
        int best = 0;
        int worst = 0;
        for (int k = 0; k < 4; k++) {
            matches[k] = computeMatchPercent(currentOutput, IDEAL_PATTERNS[k]);
            if (matches[k] > matches[best]) {
                best = k;
            }
            if (matches[k] < matches[worst]) {
                worst = k;
            }
        }

        for (int k = 0; k < 4; k++) {
            similarityFields[k].setText(String.format(Locale.US, "%.2f%%", matches[k]));
            if (matches[k] == 100.0) {
                similarityFields[k].setBackground(new Color(220, 245, 220));
            } else if (matches[k] == 0.0) {
                similarityFields[k].setBackground(new Color(255, 225, 225));
            } else if (k == best && matches[best] >= 80.0) {
                similarityFields[k].setBackground(new Color(235, 248, 225));
            } else {
                similarityFields[k].setBackground(Color.WHITE);
            }
        }

        statsLabel.setText(String.format(Locale.US,
                "Ітерацій: %d | Точність серії (шум %d%%): %d/10 (%d%%)",
                network.getLastIterations(), noisePct, exactSeriesCount, exactSeriesCount * 10));

        if (matches[best] == 100.0) {
            resultLabel.setText(String.format(Locale.US,
                    "Розпізнано: %s (повний збіг 100.0%%)", CLASS_NAMES[best]));
        } else if (matches[worst] == 0.0) {
            resultLabel.setText(String.format(Locale.US,
                    "Інвертований анти-образ до %s (збіг 0.0%%)", CLASS_NAMES[worst]));
        } else {
            resultLabel.setText(String.format(Locale.US,
                    "Хибний стан (найближчий %s — %.1f%%)", CLASS_NAMES[best], matches[best]));
        }
    }

    private class GridPanel extends JPanel {
        private final int[] data;
        private final int cellSize;

        public GridPanel(int[] data, int cellSize, boolean interactive) {
            this.data = data;
            this.cellSize = cellSize;
            setPreferredSize(new Dimension(cellSize * 10 + 1, cellSize * 10 + 1));
            if (interactive) {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        int col = e.getX() / cellSize;
                        int row = e.getY() / cellSize;
                        if (row >= 0 && row < 10 && col >= 0 && col < 10) {
                            int idx = row * 10 + col;
                            data[idx] = 1 - data[idx];
                            repaint();
                            recognizeCurrent();
                        }
                    }
                });
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    int x = c * cellSize;
                    int y = r * cellSize;
                    g.setColor(data[r * 10 + c] == 1 ? Color.BLACK : Color.WHITE);
                    g.fillRect(x, y, cellSize, cellSize);
                    g.setColor(new Color(210, 60, 60));
                    g.drawRect(x, y, cellSize, cellSize);
                }
            }
        }
    }

    public static BufferedImage renderWindowScreenshot(Main frame) {
        Container content = frame.getContentPane();
        content.validate();
        int cw = content.getWidth();
        int ch = content.getHeight();
        int titleH = 31;
        int w = cw + 2;
        int h = ch + titleH + 2;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(1, 1, w - 2, titleH);

        g2.setColor(new Color(30, 30, 30));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.drawString(frame.getTitle(), 12, 20);

        int btnW = 46;
        int closeX = w - 1 - btnW;
        int maxX = closeX - btnW;
        int minX = maxX - btnW;
        g2.setColor(new Color(60, 60, 60));
        g2.drawLine(minX + 18, 16, minX + 28, 16);
        g2.drawRect(maxX + 18, 11, 10, 10);
        g2.drawLine(closeX + 18, 11, closeX + 28, 21);
        g2.drawLine(closeX + 28, 11, closeX + 18, 21);

        g2.setColor(new Color(225, 225, 225));
        g2.drawLine(1, titleH, w - 2, titleH);

        Graphics2D cg = (Graphics2D) g2.create(1, titleH + 1, cw, ch);
        content.paint(cg);
        cg.dispose();

        g2.setColor(new Color(140, 140, 140));
        g2.drawRect(0, 0, w - 1, h - 1);
        g2.dispose();
        return img;
    }

    public static void printStatisticsTable() {
        HopfieldNetwork net = new HopfieldNetwork(100);
        net.train(IDEAL_PATTERNS);
        System.out.printf(Locale.US, "Ємність мережі (n = 100): m_max = %.2f%n", net.getMaxCapacity());
        for (int nl : NOISE_LEVELS) {
            int[] ok = new int[4];
            int[] inv = new int[4];
            for (int c = 0; c < 4; c++) {
                for (int s = 0; s < SAMPLES_PER_NOISE; s++) {
                    long seed = 1000L * (c + 1) + 100L * nl + s;
                    int[] noisy = makeNoisyPattern(IDEAL_PATTERNS[c], nl, seed);
                    int[] out = net.recognize(noisy, 50);
                    double m = computeMatchPercent(out, IDEAL_PATTERNS[c]);
                    if (m == 100.0) {
                        ok[c]++;
                    } else if (m == 0.0) {
                        inv[c]++;
                    }
                }
            }
            int sumOk = ok[0] + ok[1] + ok[2] + ok[3];
            int sumInv = inv[0] + inv[1] + inv[2] + inv[3];
            System.out.printf(Locale.US,
                    "Noise %3d%%: c1=%2d/10, c2=%2d/10, c3=%2d/10, c4=%2d/10 | Exact=%2d/40 (%5.1f%%) | Inverted=%2d/40 (%5.1f%%)%n",
                    nl, ok[0], ok[1], ok[2], ok[3], sumOk, sumOk * 100.0 / 40.0, sumInv, sumInv * 100.0 / 40.0);
        }
    }

    public static void main(String[] args) {
        if (args.length >= 1 && "--stats".equals(args[0])) {
            printStatisticsTable();
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
            if (args.length >= 2 && "--screenshots".equals(args[0])) {
                File outDir = new File(args[1]);
                outDir.mkdirs();
                Timer timer = new Timer(400, e -> {
                    try {
                        int[][] shots = {
                                {0, 0, 0},   // Class 1 (→), noise 10%, sample 1
                                {1, 1, 0},   // Class 2 (↵), noise 20%, sample 1
                                {2, 2, 0},   // Class 3 (←), noise 30%, sample 1
                                {3, 3, 0},   // Class 4 (↓), noise 35%, sample 1
                                {0, 7, 0},   // Class 1 (→), noise 60%, sample 1 (inverted)
                                {1, 11, 0}   // Class 2 (↵), noise 100%, sample 1 (inverted)
                        };
                        for (int i = 0; i < shots.length; i++) {
                            frame.selectSample(shots[i][0], shots[i][1], shots[i][2]);
                            BufferedImage img = renderWindowScreenshot(frame);
                            ImageIO.write(img, "png", new File(outDir, "win_shot_" + (i + 1) + ".png"));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        frame.dispose();
                        System.exit(0);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
}
