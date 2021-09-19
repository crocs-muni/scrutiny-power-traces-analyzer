import chartprocessing.Boundary;
import chartprocessing.ChartHelper;
import chartprocessing.HightlightingChartMouseListener;
import models.ExtractionTabModel;
import models.MainFormModel;
import muni.scrutiny.loader.DataLoader;
import muni.scrutiny.models.Trace;
import muni.scrutiny.saver.DataSaver;
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

    private MainFormModel mainFormModel = new MainFormModel();

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
    }

    public static void main(String[] argv) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainForm mainForm = new MainForm();
                JFrame mainFrame = new JFrame("Testing app");
                mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                mainFrame.setContentPane(mainForm.mainPanel);
                mainFrame.setPreferredSize(new Dimension(1260,850));
                mainFrame.pack();
                mainFrame.setVisible(true);
            }
        });
    }

    private void loadTrace() {
        try {
            String pathString = fromTraceTextField.getText();
            Path path = Paths.get(pathString);
            chartPanel.removeAll();
            Trace trace = DataLoader.importFromCsv(fromTraceTextField.getText(), 0, 1, true);
            mainFormModel.getExtractionTabModel().setCurrentTrace(trace);
            ChartPanel jfreeChartPanel = ChartHelper.plotChart(trace, path.getFileName().toString());
            jfreeChartPanel.setPreferredSize(new Dimension(chartPanel.getWidth(), chartPanel.getHeight()));
            jfreeChartPanel.addChartMouseListener(
                    new HightlightingChartMouseListener(jfreeChartPanel, fromSpinner, toSpinner, mainFormModel.getExtractionTabModel()));
            chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);
            chartPanel.validate();
            highlightButton.setEnabled(true);
        } catch (IOException exception) {

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
                extTabModel.getCurrentTrace().getTimeOnPosition(ChartHelper.modifiedToOriginalIndex(extTabModel.getFirstIndexOnChartTrace())),
                extTabModel.getCurrentTrace().getTimeOnPosition(ChartHelper.modifiedToOriginalIndex(extTabModel.getLastIndexOnChartTrace())),
                extTabModel.getFirstIndexOnChartTrace(),
                extTabModel.getLastIndexOnChartTrace());
        List<Boundary> boundariesList = new ArrayList<>();
        boundariesList.add(boundary);
        ChartPanel jfreeChartPanel = (ChartPanel) chartPanel.getComponent(0);
        ChartHelper.highlightChart(jfreeChartPanel, boundariesList);
    }

    private void saveTrace() {
        ExtractionTabModel extTabModel = mainFormModel.getExtractionTabModel();
        try {
            DataSaver.exportToCsv(extTabModel.getCurrentTrace(), saveToTextField.getText(), extTabModel.getFirstIndexOnChartTrace(), extTabModel.getLastIndexOnChartTrace());
        } catch (IOException exception) {

        }
    }
}
