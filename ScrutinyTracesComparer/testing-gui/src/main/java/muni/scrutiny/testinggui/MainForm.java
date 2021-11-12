package muni.scrutiny.testinggui;

import muni.scrutiny.charts.models.Boundary;
import muni.scrutiny.similaritysearch.pipelines.base.PreprocessingResult;
import muni.scrutiny.testinggui.chartprocessing.HightlightingChartMouseListener;
import muni.scrutiny.testinggui.chartprocessing.UITracePlotter;
import muni.scrutiny.testinggui.models.ExtractionTabModel;
import muni.scrutiny.testinggui.models.MainFormModel;
import muni.scrutiny.testinggui.models.resources.ComboItemModel;
import muni.scrutiny.testinggui.testingpipelines.TraceResamplingPipeline;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.loader.DataLoader;
import muni.scrutiny.traces.models.Trace;
import muni.scrutiny.traces.saver.DataSaver;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainForm {
    private JPanel mainPanel;

    private JTabbedPane mainPane;

    // Extraction part
    private JTextField fromTraceTextField;
    private JButton loadButton;
    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JButton highlightButton;
    private JTextField saveToTextField;
    private JButton saveButton;
    private JPanel chartPanel;

    // Visualization part
    private JTextField preprocessTraceTextField;
    private JComboBox pipelinesComboBox;
    private JButton executeButton;
    private JPanel beforeChartPanel;
    private JPanel afterChartPanel;

    private final MainFormModel mainFormModel = new MainFormModel();

    public MainForm() {
        initExtractionTab();
        initVisualizationTab();
    }

    public static void main(String[] argv) {
        EventQueue.invokeLater(() -> {
            MainForm mainForm = new MainForm();
            JFrame mainFrame = new JFrame("Testing app");
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setContentPane(mainForm.mainPanel);
            mainFrame.setPreferredSize(new Dimension(1260, 850));
            mainFrame.pack();
            mainFrame.setVisible(true);
        });
    }

    private void initExtractionTab() {
        loadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                loadTrace();
            }
        });

        highlightButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                highlightChartArea();
            }
        });

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                saveTrace();
            }
        });

        fromSpinner.addChangeListener(e -> {
            double value = Double.parseDouble(fromSpinner.getValue().toString());
            int index = mainFormModel.getExtractionTabModel().getCurrentTrace().getIndexOfTimeValue(value);
            mainFormModel.getExtractionTabModel().setFirstIndexOnChartTrace(index);
        });
        toSpinner.addChangeListener(e -> {
            double value = Double.parseDouble(fromSpinner.getValue().toString());
            int index = mainFormModel.getExtractionTabModel().getCurrentTrace().getIndexOfTimeValue(value);
            mainFormModel.getExtractionTabModel().setLastIndexOnChartTrace(index);
        });
    }

    private void initVisualizationTab() {
        pipelinesComboBox.addItem(new ComboItemModel("-", ""));
        pipelinesComboBox.addItem(new ComboItemModel("Resampling Pipeline", "TraceResamplingPipeline"));
        executeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                executeProcessing();
            }
        });
    }

    private void loadTrace() {
        try {
            Path path = Paths.get(fromTraceTextField.getText());
            chartPanel.removeAll();
            Trace trace = DataLoader.importFromCsv(path, DataManager.DEFAULT_TIME_COLUMN, DataManager.DEFAULT_VOLTAGE_COLUMN, false);
            mainFormModel.getExtractionTabModel().setCurrentTrace(trace);
            UITracePlotter uiTracePlotter = new UITracePlotter(trace);
            Dimension panelSize = new Dimension(chartPanel.getWidth(), chartPanel.getHeight());
            ChartPanel jfreeChartPanel = uiTracePlotter.createChartPanel("Traces chart", "Time", "Voltage", panelSize);
            jfreeChartPanel.addChartMouseListener(
                    new HightlightingChartMouseListener(jfreeChartPanel, fromSpinner, toSpinner, mainFormModel.getExtractionTabModel()));
            chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);
            chartPanel.validate();
            highlightButton.setEnabled(true);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(mainPanel, "Could not load trace");
        }
    }

    private void highlightChartArea() {
        if (!highlightButton.isEnabled()) {
            return;
        }

        ExtractionTabModel extTabModel = mainFormModel.getExtractionTabModel();
        if (extTabModel.getFirstIndexOnChartTrace() == null
                || extTabModel.getFirstIndexOnChartTrace() <= 0
                || extTabModel.getLastIndexOnChartTrace() == null
                || extTabModel.getLastIndexOnChartTrace() <= 0
                || extTabModel.getLastIndexOnChartTrace() <= extTabModel.getFirstIndexOnChartTrace()) {
            JOptionPane.showMessageDialog(mainPanel, "Could not highlight area.");
            return;
        }

        double[] time = extTabModel.getCurrentTrace().getTime(false);
        Boundary boundary = new Boundary(
                time[extTabModel.getFirstIndexOnChartTrace()],
                time[extTabModel.getLastIndexOnChartTrace()],
                extTabModel.getFirstIndexOnChartTrace(),
                extTabModel.getLastIndexOnChartTrace());
        List<Boundary> boundariesList = new ArrayList<>();
        boundariesList.add(boundary);
        ChartPanel jfreeChartPanel = (ChartPanel) chartPanel.getComponent(0);
        UITracePlotter.highlightChart(jfreeChartPanel, boundariesList);
    }

    private void saveTrace() {
        ExtractionTabModel extTabModel = mainFormModel.getExtractionTabModel();
        try {
            DataSaver.exportToCsv(extTabModel.getCurrentTrace(), Paths.get(saveToTextField.getText()), extTabModel.getFirstIndexOnChartTrace(), extTabModel.getLastIndexOnChartTrace());
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(mainPanel, "Could not save trace");
        }
    }

    private void executeProcessing() {
        ComboItemModel comboItemModel = (ComboItemModel) pipelinesComboBox.getSelectedItem();
        if (comboItemModel == null || comboItemModel.getValue() == null || comboItemModel.getValue().equals("")) {
            JOptionPane.showMessageDialog(mainPanel, "No operation was selected");
        } else if (TraceResamplingPipeline.class.getName().contains(comboItemModel.getValue())) {
            try {
                // Load and show trace
                Path path = Paths.get(preprocessTraceTextField.getText());
                Trace trace = DataLoader.importFromCsv(path, DataManager.DEFAULT_TIME_COLUMN, DataManager.DEFAULT_VOLTAGE_COLUMN, false);
                beforeChartPanel.removeAll();
                afterChartPanel.removeAll();
                UITracePlotter uiTracePlotter1 = new UITracePlotter(trace);
                Dimension panelSize = new Dimension(beforeChartPanel.getWidth(), beforeChartPanel.getHeight());
                ChartPanel jfreeChartPanel1 = uiTracePlotter1.createChartPanel("Traces chart - " + trace.getSamplingFrequency(), "Time", "Voltage", panelSize);
                beforeChartPanel.add(jfreeChartPanel1, BorderLayout.CENTER);
                beforeChartPanel.validate();

                // Preprocess and show trace
                TraceResamplingPipeline trp = new TraceResamplingPipeline((int) (trace.getSamplingFrequency() * 5), 1);
                PreprocessingResult pr = trp.preprocess(trace);
                UITracePlotter uiTracePlotter2 = new UITracePlotter(pr.getPreprocessedTrace());
                ChartPanel jfreeChartPanel2 = uiTracePlotter2.createChartPanel("Traces chart - " + pr.getPreprocessedTrace().getSamplingFrequency(), "Time", "Voltage", panelSize);
                afterChartPanel.add(jfreeChartPanel2, BorderLayout.CENTER);
                afterChartPanel.validate();
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(mainPanel, "Could not load trace");
            }
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainPane = new JTabbedPane();
        Font mainPaneFont = this.$$$getFont$$$("Segoe UI", -1, 14, mainPane.getFont());
        if (mainPaneFont != null) mainPane.setFont(mainPaneFont);
        mainPanel.add(mainPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(1230, 790), new Dimension(1230, 790), new Dimension(1230, 790), 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(12, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPane.addTab("Extraction", panel1);
        final JLabel label1 = new JLabel();
        label1.setText("Trace path");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        fromTraceTextField = new JTextField();
        panel1.add(fromTraceTextField, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        loadButton = new JButton();
        loadButton.setText("Load");
        panel1.add(loadButton, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Extract to");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(780, 25), new Dimension(780, 25), new Dimension(780, 25), 0, false));
        fromSpinner = new JSpinner();
        panel1.add(fromSpinner, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Extract from");
        panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(780, 25), new Dimension(780, 25), new Dimension(780, 25), 0, false));
        toSpinner = new JSpinner();
        panel1.add(toSpinner, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        highlightButton = new JButton();
        highlightButton.setText("Highlight");
        panel1.add(highlightButton, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout(0, 0));
        chartPanel.setBackground(new Color(-1));
        panel1.add(chartPanel, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 420), new Dimension(-1, 420), new Dimension(-1, 420), 0, false));
        chartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Save to");
        panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(780, 25), new Dimension(780, 25), new Dimension(780, 25), 0, false));
        saveToTextField = new JTextField();
        panel1.add(saveToTextField, new com.intellij.uiDesigner.core.GridConstraints(10, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(150, 25), new Dimension(-1, 25), 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel1.add(saveButton, new com.intellij.uiDesigner.core.GridConstraints(11, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(11, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPane.addTab("Visualization", panel2);
        final JLabel label5 = new JLabel();
        label5.setText("Trace path");
        panel2.add(label5, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        preprocessTraceTextField = new JTextField();
        panel2.add(preprocessTraceTextField, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        pipelinesComboBox = new JComboBox();
        panel2.add(pipelinesComboBox, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        executeButton = new JButton();
        executeButton.setText("Execute");
        panel2.add(executeButton, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        beforeChartPanel = new JPanel();
        beforeChartPanel.setLayout(new BorderLayout(0, 0));
        beforeChartPanel.setBackground(new Color(-1));
        panel2.add(beforeChartPanel, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 300), new Dimension(-1, 300), new Dimension(-1, 300), 0, false));
        beforeChartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        afterChartPanel = new JPanel();
        afterChartPanel.setLayout(new BorderLayout(0, 0));
        afterChartPanel.setBackground(new Color(-1));
        panel2.add(afterChartPanel, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 300), new Dimension(-1, 300), new Dimension(-1, 300), 0, false));
        afterChartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        panel2.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
