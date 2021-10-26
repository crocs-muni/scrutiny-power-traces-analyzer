package muni.scrutiny.charts;

import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;
import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.List;

public class TracePlotter {
    public static final float CHART_THICKNESS = 1.2f;
    public static final Color RED = new Color(219,68,55);
    public static final Color GREEN = new Color(15, 157, 88);
    public static final Color BLUE = new Color(66, 133, 244);
    public static final Color YELLOW = new Color(244, 180, 0);

    private final List<ChartTrace> traces;

    public TracePlotter(Trace trace) {
        this.traces = new ArrayList<>();
        traces.add(new ChartTrace(trace, RED));
    }

    public TracePlotter(ChartTrace chartTrace) {
        this.traces = new ArrayList<>();
        traces.add(chartTrace);
    }

    public TracePlotter(List<ChartTrace> traces) {
        this.traces = traces;
    }

    public List<XYSeries> createXYSeries() {
        List<XYSeries> xySeriesList = new ArrayList<>();
        for (ChartTrace trace : traces) {
            xySeriesList.add(createXYSeries(trace));
        }

        return xySeriesList;
    }

    public XYSeriesCollection createXYSeriesCollection() {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        List<XYSeries> xySeriesList = createXYSeries();
        for (XYSeries xySeries : xySeriesList) {
            xySeriesCollection.addSeries(xySeries);
        }

        return xySeriesCollection;
    }

    public JFreeChart createXYLineChart(String name, String xAxisLabel, String yAxisLabel) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                name,
                xAxisLabel,
                yAxisLabel,
                createXYSeriesCollection(),
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        initBaseProperties(chart);
        return chart;
    }

    public void initBaseProperties(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        for (int i = 0; i < plot.getDatasetCount(); i++) {
            NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis(i);
            rangeAxis.setAutoRangeIncludesZero(false);
            NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis(i);
            domainAxis.setAutoRangeIncludesZero(false);
            XYItemRenderer renderer = plot.getRenderer(i);
            renderer.setSeriesStroke(i, new BasicStroke(CHART_THICKNESS));
            renderer.setSeriesPaint(i, traces.get(i).getColor());
        }
    }

    private static XYSeries createXYSeries(ChartTrace trace) {
        Trace t = trace.getTrace();
        XYSeries xySeries = new XYSeries(t.getName(), false, true);
        xySeries.setMaximumItemCount(t.getDataCount());
        double[] voltage = t.getVoltage();
        double[] time = t.getTime(false);
        for (int i = 0; i < t.getDataCount(); i++) {
            xySeries.add(time[i], voltage[i]);
        }

        return xySeries;
    }
}
