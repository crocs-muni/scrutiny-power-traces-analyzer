package muni.scrutiny.testinggui;

import com.google.gson.Gson;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.Boundary;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.module.pipelines.testing.*;
import muni.scrutiny.similaritysearch.pipelines.base.PreprocessingResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.testinggui.chartprocessing.HightlightingChartMouseListener;
import muni.scrutiny.testinggui.chartprocessing.UITracePlotter;
import muni.scrutiny.testinggui.models.ExtractionTabModel;
import muni.scrutiny.testinggui.models.MainFormModel;
import muni.scrutiny.testinggui.models.resources.ComboItemModel;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.loader.DataLoader;
import muni.scrutiny.traces.models.Trace;
import muni.scrutiny.traces.saver.DataSaver;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
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
    private JPanel visualizationChartPanel;
    private JTextField customDataJson;

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
            double value = Double.parseDouble(toSpinner.getValue().toString());
            int index = mainFormModel.getExtractionTabModel().getCurrentTrace().getIndexOfTimeValue(value);
            mainFormModel.getExtractionTabModel().setLastIndexOnChartTrace(index);
        });
    }

    private void initVisualizationTab() {
        pipelinesComboBox.addItem(new ComboItemModel("-", ""));
        pipelinesComboBox.addItem(new ComboItemModel("Resampling Pipeline", ResamplingPipeline.class.getName()));
        pipelinesComboBox.addItem(new ComboItemModel("Up/Downsampling Pipeline", "Up/DownsamplingPipeline"));
        pipelinesComboBox.addItem(new ComboItemModel("Scaling/Offsetting Pipeline", "Scaling/OffsettingPipeline"));
        pipelinesComboBox.addItem(new ComboItemModel("Butterworth Pipeline", ButterworthFilterPipeline.class.getName()));
        pipelinesComboBox.addItem(new ComboItemModel("Chebyshev Pipeline", ChebyshevFilterPipeline.class.getName()));
        pipelinesComboBox.addItem(new ComboItemModel("Bessel Pipeline", BesselFilterPipeline.class.getName()));
        pipelinesComboBox.addItem(new ComboItemModel("Butterworth and Chebyshev Pipeline", "ButterworthAndChebyshevFilterPipeline"));
        pipelinesComboBox.addItem(new ComboItemModel("Exponential smoother Pipeline", ExponentialSmootherPipeline.class.getName()));
        pipelinesComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String value = ((ComboItemModel) pipelinesComboBox.getSelectedItem()).getValue();
                if (ResamplingPipeline.class.getName().contains(value)) {
                    customDataJson.setText("{\"sampling_frequency\":1000000, \"interval_radius\":10}");
                } else if (value.contains("Up/DownsamplingPipeline")) {
                    customDataJson.setText("{\"before\":{\"sampling_frequency\":5000000, \"interval_radius\":10}, \"after\":{\"sampling_frequency\":20000000, \"interval_radius\":10}}");
                } else if (value.contains("Scaling/OffsettingPipeline")) {
                    customDataJson.setText("{\"scale\":2, \"offset\":10}");
                } else if (ButterworthFilterPipeline.class.getName().contains(value)) {
                    customDataJson.setText("{\"cutoff_frequency\":10000}");
                } else if (ChebyshevFilterPipeline.class.getName().contains(value)) {
                    customDataJson.setText("{\"cutoff_frequency\":10000}");
                } else if (value.contains("ButterworthAndChebyshevFilterPipeline")) {
                    customDataJson.setText("{\"cutoff_frequency\":10000}");
                } else if (BesselFilterPipeline.class.getName().contains(value)) {
                    customDataJson.setText("{\"cutoff_frequency\":10000}");
                } else if (ExponentialSmootherPipeline.class.getName().contains(value)) {
                    customDataJson.setText("{\"alpha\":1}");
                } else {
                    customDataJson.setText("");
                }
            }
        });
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
        visualizationChartPanel.removeAll();
        ComboItemModel comboItemModel = (ComboItemModel) pipelinesComboBox.getSelectedItem();
        if (comboItemModel == null || comboItemModel.getValue() == null || comboItemModel.getValue().equals("")) {
            JOptionPane.showMessageDialog(mainPanel, "No operation was selected");
        } else {
            try {
                // Load and show trace
                Path path = Paths.get(preprocessTraceTextField.getText());
                Trace trace = DataLoader.importFromCsv(path, DataManager.DEFAULT_TIME_COLUMN, DataManager.DEFAULT_VOLTAGE_COLUMN, false);
                TracePipeline trp = null;
                String subtitleMessage = "";
                List<ChartTrace> chartTraces = new ArrayList<>();

                if (ResamplingPipeline.class.getName().contains(comboItemModel.getValue())) {
                    ResamplingPipelineJson rpj = (new Gson()).fromJson(customDataJson.getText(), ResamplingPipelineJson.class);
                    trp = new ResamplingPipeline(rpj.sampingFrequency, rpj.intervalRadius);
                    subtitleMessage = "Resampling from sampling frequency " + trace.getSamplingFrequency() + " to " + rpj.sampingFrequency;
                } else if (comboItemModel.getValue().contains("Up/DownsamplingPipeline")) {
                    ResamplingPipelinesJson rpjs = (new Gson()).fromJson(customDataJson.getText(), ResamplingPipelinesJson.class);
                    trp = new ResamplingPipeline(rpjs.before.sampingFrequency, rpjs.before.intervalRadius);
                    ResamplingPipeline atrp = new ResamplingPipeline(rpjs.after.sampingFrequency, rpjs.after.intervalRadius);
                    ChartTrace ctAdd = new ChartTrace(atrp.preprocess(trace).getPreprocessedTrace(), TracePlotter.GRAY, new Ellipse2D.Double(-7, -7, 14, 14));
                    ctAdd.setDisplayName(trace.getDisplayName() + "-after-upsample");
                    ctAdd.setOrder(30);
                    chartTraces.add(ctAdd);
                } else if (comboItemModel.getValue().contains("Scaling/OffsettingPipeline")) {
                    ScalingOffsettingPipelineJson sopj = (new Gson()).fromJson(customDataJson.getText(), ScalingOffsettingPipelineJson.class);
                    trp = new ScalingPipeline(sopj.scale);
                    OffsettingPipeline otrp = new OffsettingPipeline(sopj.offset);
                    ChartTrace ctAdd = new ChartTrace(otrp.preprocess(trace).getPreprocessedTrace(), TracePlotter.GRAY, new BasicStroke(0.5f));
                    ctAdd.setDisplayName(trace.getDisplayName() + "-offset");
                    ctAdd.setOrder(30);
                    chartTraces.add(ctAdd);
                } else if (ButterworthFilterPipeline.class.getName().contains(comboItemModel.getValue())) {
                    ButterworthFilterPipelineJson bfpj = (new Gson()).fromJson(customDataJson.getText(), ButterworthFilterPipelineJson.class);
                    trp = new ButterworthFilterPipeline(bfpj.cutoffFrequency);
                    subtitleMessage = "Filtering with cutoff frequency " + bfpj.cutoffFrequency;
                } else if (ChebyshevFilterPipeline.class.getName().contains(comboItemModel.getValue())) {
                    ButterworthFilterPipelineJson bfpj = (new Gson()).fromJson(customDataJson.getText(), ButterworthFilterPipelineJson.class);
                    trp = new ChebyshevFilterPipeline(bfpj.cutoffFrequency);
                    subtitleMessage = "Filtering with cutoff frequency " + bfpj.cutoffFrequency;
                } else if (BesselFilterPipeline.class.getName().contains(comboItemModel.getValue())) {
                    ButterworthFilterPipelineJson bfpj = (new Gson()).fromJson(customDataJson.getText(), ButterworthFilterPipelineJson.class);
                    trp = new BesselFilterPipeline(bfpj.cutoffFrequency);
                    subtitleMessage = "Filtering with cutoff frequency " + bfpj.cutoffFrequency;
                } else if (comboItemModel.getValue().contains("ButterworthAndChebyshevFilterPipeline")) {
                    ButterworthFilterPipelineJson bfpj = (new Gson()).fromJson(customDataJson.getText(), ButterworthFilterPipelineJson.class);
                    trp = new ChebyshevFilterPipeline(bfpj.cutoffFrequency);
                    ChartTrace ctAdd = new ChartTrace(new ButterworthFilterPipeline(bfpj.cutoffFrequency).preprocess(trace).getPreprocessedTrace(), TracePlotter.GRAY, new BasicStroke(0.5f));
                    ctAdd.setDisplayName(trace.getDisplayName() + "-butterworth");
                    ctAdd.setOrder(30);
                    chartTraces.add(ctAdd);
                    subtitleMessage = "Filtering with cutoff frequency " + bfpj.cutoffFrequency;
                } else if (ExponentialSmootherPipeline.class.getName().contains(comboItemModel.getValue())) {
                    ExponentialSmootherPipelineJson bfpj = (new Gson()).fromJson(customDataJson.getText(), ExponentialSmootherPipelineJson.class);
                    trp = new ExponentialSmootherPipeline(bfpj.alpha);
                    subtitleMessage = "Smoothing with alpha " + bfpj.alpha;
                }

                PreprocessingResult pr = trp.preprocess(trace);
                ChartTrace ctBefore = new ChartTrace(trace, TracePlotter.BLUE, new Ellipse2D.Double(-7, -7, 14, 14));
                ChartTrace ctAfter = new ChartTrace(pr.getPreprocessedTrace(), TracePlotter.ORANGE, new Ellipse2D.Double(-7, -7, 14, 14));
                ctBefore.setDisplayName(trace.getDisplayName() + "-before");
                ctBefore.setOrder(10);
                ctAfter.setDisplayName(trace.getDisplayName() + "-after");
                ctAfter.setOrder(20);
                chartTraces.add(ctBefore);
                chartTraces.add(ctAfter);
                UITracePlotter uiTracePlotter = new UITracePlotter(chartTraces);
                Dimension panelSize = new Dimension(visualizationChartPanel.getWidth(), visualizationChartPanel.getHeight());
                JFreeChart jfc = uiTracePlotter.createXYLineChart(trace.getDisplayName() + " " + comboItemModel.getKey(), trace.getDisplayTimeUnit(), trace.getDisplayVoltageUnit());
                jfc.addSubtitle(new TextTitle(subtitleMessage));
                ChartPanel cp = uiTracePlotter.createChartPanel(jfc, panelSize);
                visualizationChartPanel.add(cp, BorderLayout.CENTER);
                visualizationChartPanel.validate();
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
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainPane = new JTabbedPane();
        Font mainPaneFont = this.$$$getFont$$$("Segoe UI", -1, 14, mainPane.getFont());
        if (mainPaneFont != null) mainPane.setFont(mainPaneFont);
        mainPanel.add(mainPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(1230, 790), new Dimension(1230, 790), new Dimension(1230, 790), 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(12, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPane.addTab("Extraction", panel1);
        final JLabel label1 = new JLabel();
        label1.setText("Trace path");
        panel1.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        fromTraceTextField = new JTextField();
        panel1.add(fromTraceTextField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        loadButton = new JButton();
        loadButton.setText("Load");
        panel1.add(loadButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Extract to");
        panel1.add(label2, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(780, 25), new Dimension(780, 25), new Dimension(780, 25), 0, false));
        fromSpinner = new JSpinner();
        panel1.add(fromSpinner, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Extract from");
        panel1.add(label3, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(780, 25), new Dimension(780, 25), new Dimension(780, 25), 0, false));
        toSpinner = new JSpinner();
        panel1.add(toSpinner, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        highlightButton = new JButton();
        highlightButton.setText("Highlight");
        panel1.add(highlightButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout(0, 0));
        chartPanel.setBackground(new Color(-1));
        panel1.add(chartPanel, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 420), new Dimension(-1, 420), new Dimension(-1, 420), 0, false));
        chartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Save to");
        panel1.add(label4, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(780, 25), new Dimension(780, 25), new Dimension(780, 25), 0, false));
        saveToTextField = new JTextField();
        panel1.add(saveToTextField, new GridConstraints(10, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(150, 25), new Dimension(-1, 25), 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel1.add(saveButton, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPane.addTab("Visualization", panel2);
        final JLabel label5 = new JLabel();
        label5.setText("Trace path");
        panel2.add(label5, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        preprocessTraceTextField = new JTextField();
        panel2.add(preprocessTraceTextField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        pipelinesComboBox = new JComboBox();
        panel2.add(pipelinesComboBox, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        executeButton = new JButton();
        executeButton.setText("Execute");
        panel2.add(executeButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        visualizationChartPanel = new JPanel();
        visualizationChartPanel.setLayout(new BorderLayout(0, 0));
        visualizationChartPanel.setBackground(new Color(-1));
        panel2.add(visualizationChartPanel, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 575), new Dimension(-1, 575), new Dimension(-1, 575), 0, false));
        visualizationChartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final Spacer spacer4 = new Spacer();
        panel2.add(spacer4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        customDataJson = new JTextField();
        panel2.add(customDataJson, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
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
