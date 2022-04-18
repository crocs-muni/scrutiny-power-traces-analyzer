package muni.scrutiny.charts;

import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.StandardTickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TracePlotter {
    public static final float CONNECTION_THICKNESS = 0.7f;
    public static final float CHART_THICKNESS = 1.2f;

    public static final Stroke basicChartStroke = new BasicStroke(CHART_THICKNESS);
    public static final Stroke thinChartStroke = new BasicStroke(0.5f);
    public static final Stroke basicDashedStroke = new BasicStroke(
            CHART_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            CHART_THICKNESS, new float[] {6.0f, 6.0f}, 0.0f);
    public static final Stroke connectionDashedStroke = new BasicStroke(
            CONNECTION_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            CONNECTION_THICKNESS, new float[] {6.0f, 6.0f}, 0.0f);

    public static final Font f1BoldFont = new Font("F1", Font.BOLD, 16);

    public static final Color RED = new Color(219,68,55);
    public static final Color GREEN = new Color(15, 157, 88);
    public static final Color LIGHT_GREEN = new Color(127, 255, 148);
    public static final Color GRAY = new Color(125, 125, 125);
    public static final Color BLUE = new Color(66, 133, 244);
    public static final Color YELLOW = new Color(244, 180, 0);
    public static final Color ORANGE = new Color(255, 165, 0);
    public static final Color BLACK = new Color(0,0,0);

    private final List<ChartTrace> traces;
    private final List<XYSeries> additionalSeries;

    public TracePlotter(Trace trace) {
        this.traces = new ArrayList<>();
        this.additionalSeries = new ArrayList<>();
        traces.add(new ChartTrace(trace, BLUE));
    }

    public TracePlotter(ChartTrace chartTrace) {
        this.traces = new ArrayList<>();
        traces.add(chartTrace);
        this.additionalSeries = new ArrayList<>();
    }

    public TracePlotter(List<ChartTrace> traces) {
        Collections.sort(traces, (ct1, ct2) -> Integer.compare(ct1.getOrder(), ct2.getOrder()));
        this.traces = traces;
        this.additionalSeries = new ArrayList<>();
    }

    public TracePlotter(List<ChartTrace> traces, List<XYSeries> additionalSeries) {
        Collections.sort(traces, (ct1, ct2) -> Integer.compare(ct1.getOrder(), ct2.getOrder()));
        this.traces = traces;
        this.additionalSeries = additionalSeries;
    }

    public JFreeChart assignSeriesToChart(JFreeChart chart) {
        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        int index = 0;
        for (ChartTrace trace : traces) {
            xyPlot.setDataset(index, new XYSeriesCollection(createXYSeries(trace)));
            XYLineAndShapeRenderer renderer = new DefaultXYItemRenderer();
            renderer.setSeriesShapesVisible(0, trace.getShape() != null);
            if (trace.getShape() != null) {
                renderer.setSeriesShape(0, trace.getShape());
            }

            renderer.setSeriesLinesVisible(0, trace.getStroke() != null);
            if (trace.getStroke() != null) {
                renderer.setSeriesStroke(0, trace.getStroke());
            }

            renderer.setSeriesPaint(0, trace.getColor());
            renderer.setSeriesVisibleInLegend(index, true);
            xyPlot.setRenderer(index, renderer);
            index++;
        }

        for (XYSeries xySeries : additionalSeries) {
            xyPlot.setDataset(index, new XYSeriesCollection(xySeries));
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setBaseShapesVisible(false);
            renderer.setSeriesStroke(0, connectionDashedStroke);
            renderer.setSeriesPaint(0, BLACK);
            renderer.setSeriesVisibleInLegend(0, false);
            xyPlot.setRenderer(index, renderer);
            index++;
        }

        return chart;
    }

    public JFreeChart createXYLineChart(String name, String xAxisLabel, String yAxisLabel) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                name,
                xAxisLabel,
                yAxisLabel,
                null,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        assignSeriesToChart(chart);
        initBaseProperties(chart);
        return chart;
    }

    public void initBaseProperties(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);
        NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis(0);
        rangeAxis.setAutoRangeIncludesZero(false);
        NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis(0);
        domainAxis.setAutoRangeIncludesZero(false);
    }

    public static Color getColor(double power)
    {
        double H = 0; // Hue
        double S = power; // Saturation
        double B = 1.9; // Brightness

        return Color.getHSBColor((float)H, (float)S, (float)B);
    }

    private static XYSeries createXYSeries(ChartTrace trace) {
        Trace t = trace.getTrace();
        XYSeries xySeries = new XYSeries(trace.getDisplayName() == null ? t.getDisplayName() : trace.getDisplayName(), false, true);
        xySeries.setMaximumItemCount(t.getDataCount());
        double[] voltage = t.getVoltage();
        double[] time = t.getTime(false, trace.getIndexOffset());
        for (int i = 0; i < t.getDataCount(); i++) {
            xySeries.add(time[i], voltage[i]);
        }

        return xySeries;
    }
}
