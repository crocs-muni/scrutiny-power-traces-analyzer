package muni.scrutiny.testinggui;

import com.google.gson.Gson;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.Boundary;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.module.pipelines.testing.*;
import muni.scrutiny.similaritysearch.pipelines.base.PreprocessingResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ChebyshevLowpassFilter;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ExponentialSmoother;
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        pipelinesComboBox.addItem(new ComboItemModel("Butterworth Pipeline", ButterworthFilterPipeline.class.getName()));
        pipelinesComboBox.addItem(new ComboItemModel("Chebyshev Pipeline", ChebyshevFilterPipeline.class.getName()));
        pipelinesComboBox.addItem(new ComboItemModel("Bessel Pipeline", BesselFilterPipeline.class.getName()));
        pipelinesComboBox.addItem(new ComboItemModel("Butterworth and Chebyshev Pipeline", "ButterworthAndChebyshevFilterPipeline"));
        pipelinesComboBox.addItem(new ComboItemModel("Exponential smoother Pipeline", ExponentialSmootherPipeline.class.getName()));
        pipelinesComboBox.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String value = ((ComboItemModel) pipelinesComboBox.getSelectedItem()).getValue();
                if (ResamplingPipeline.class.getName().contains(value)) {
                    customDataJson.setText("{\"sampling_frequency\":1000000, \"radius\":10}");
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
                ChartTrace ctBefore = new ChartTrace(trace, TracePlotter.BLUE, new BasicStroke(1f));
                ChartTrace ctAfter = new ChartTrace(pr.getPreprocessedTrace(), TracePlotter.ORANGE, new BasicStroke(2f));
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

}
