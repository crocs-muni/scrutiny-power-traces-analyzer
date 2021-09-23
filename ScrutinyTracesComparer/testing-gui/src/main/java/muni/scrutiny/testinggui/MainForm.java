package muni.scrutiny.testinggui;

import muni.scrutiny.charts.models.Boundary;
import muni.scrutiny.testinggui.chartprocessing.HightlightingChartMouseListener;
import muni.scrutiny.testinggui.chartprocessing.UITracePlotter;
import muni.scrutiny.testinggui.models.ExtractionTabModel;
import muni.scrutiny.testinggui.models.MainFormModel;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.loader.DataLoader;
import muni.scrutiny.traces.models.Trace;
import muni.scrutiny.traces.saver.DataSaver;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
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
    private JTextField fromTraceTextField;
    private JButton loadButton;
    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JButton highlightButton;
    private JTextField saveToTextField;
    private JButton saveButton;
    private JPanel chartPanel;

    private final MainFormModel mainFormModel = new MainFormModel();

    public MainForm() {
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
        fromSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = Double.valueOf(fromSpinner.getValue().toString());
                int index = mainFormModel.getExtractionTabModel().getCurrentTrace().getIndexOfTimeValue(value);
                mainFormModel.getExtractionTabModel().setFirstIndexOnChartTrace(index);
            }
        });
        toSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = Double.valueOf(fromSpinner.getValue().toString());
                int index = mainFormModel.getExtractionTabModel().getCurrentTrace().getIndexOfTimeValue(value);
                mainFormModel.getExtractionTabModel().setLastIndexOnChartTrace(index);
            }
        });
    }

    public static void main(String[] argv) {
        EventQueue.invokeLater(() -> {
            MainForm mainForm = new MainForm();
            JFrame mainFrame = new JFrame("Testing app");
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setContentPane(mainForm.mainPanel);
            mainFrame.setPreferredSize(new Dimension(1260,850));
            mainFrame.pack();
            mainFrame.setVisible(true);
        });
    }

    private void loadTrace() {
        try {
            Path path = Paths.get(fromTraceTextField.getText());
            chartPanel.removeAll();
            Trace trace = DataLoader.importFromCsv(path, DataManager.DEFAULT_TIME_COLUMN, DataManager.DEFAULT_VOLTAGE_COLUMN, true);
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

        Boundary boundary = new Boundary(
                extTabModel.getCurrentTrace().getTimeOnPosition(extTabModel.getFirstIndexOnChartTrace()),
                extTabModel.getCurrentTrace().getTimeOnPosition(extTabModel.getLastIndexOnChartTrace()),
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
}
