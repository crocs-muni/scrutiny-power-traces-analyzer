package muni.scrutiny.testinggui.chartprocessing;

import java.awt.*;
import java.util.Collection;
import java.util.List;

import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.Boundary;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

/**
 * This class contains static methods used for plotting and highlighting charts.
 *
 * @author Martin Podhora
 */
public class UITracePlotter extends TracePlotter {
    public UITracePlotter(Trace trace) {
        super(trace);
    }

    public UITracePlotter(ChartTrace trace) {
        super(trace);
    }

    public UITracePlotter(List<ChartTrace> traces) {
        super(traces);
    }

    public ChartPanel createChartPanel(String name, String xAxisLabel, String yAxisLabel, Dimension panelSize) {
        JFreeChart chart = createXYLineChart(name, xAxisLabel, yAxisLabel);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(panelSize);
        return panel;
    }

    /**
     * Can highligh trace part on the chart but it expects only one xyseries
     *
     * @param chartPanel panel where is chart
     * @param highlightingBounds bounds in which trace should be highlighted
     */
    public static void highlightChart(ChartPanel chartPanel, Collection<Boundary> highlightingBounds) {
        XYPlot plot = chartPanel.getChart().getXYPlot();
        XYDataset dataset = plot.getDataset();

        XYItemRenderer renderer = new StandardXYItemRenderer() {
            @Override
            public Paint getItemPaint(int series, int item) {
                double value = dataset.getXValue(series, item);
                if (highlightingBounds
                        .stream()
                        .anyMatch((Boundary boundary) -> value > boundary.getLowerBound() && value < boundary.getUpperBound())) {
                    return GREEN;
                } else {
                    return RED;
                }
            }
        };

        renderer.setSeriesStroke(0, new BasicStroke(CHART_THICKNESS));

        plot.setRenderer(renderer);
    }
}