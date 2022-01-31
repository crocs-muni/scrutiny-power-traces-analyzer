package muni.scrutiny.testinggui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import muni.scrutiny.charts.models.Boundary;
import muni.scrutiny.module.pipelines.testing.TraceDFTPipeline;
import muni.scrutiny.module.pipelines.testing.TraceResamplingPipeline;
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
            double value = Double.parseDouble(toSpinner.getValue().toString());
            int index = mainFormModel.getExtractionTabModel().getCurrentTrace().getIndexOfTimeValue(value);
            mainFormModel.getExtractionTabModel().setLastIndexOnChartTrace(index);
        });
    }

    private void initVisualizationTab() {
        pipelinesComboBox.addItem(new ComboItemModel("-", ""));
        pipelinesComboBox.addItem(new ComboItemModel("Resampling Pipeline", "TraceResamplingPipeline"));
        pipelinesComboBox.addItem(new ComboItemModel("DFT Pipeline", "TraceDFTPipeline"));
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
        } else {
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
                TracePipeline trp = null;
                if (TraceResamplingPipeline.class.getName().contains(comboItemModel.getValue())) {
                    trp = new TraceResamplingPipeline((int) (trace.getSamplingFrequency() * 5), 1);
                } else if (TraceDFTPipeline.class.getName().contains(comboItemModel.getValue())) {
                    trp = new TraceDFTPipeline();
                }

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

}
