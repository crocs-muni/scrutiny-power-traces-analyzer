package muni.scrutiny.testinggui.chartprocessing;
import javax.swing.JSpinner;

import muni.scrutiny.testinggui.models.ExtractionTabModel;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class is a simple mouse listener used in chart panel.
 * If one of JRadioButtons is marked, then clicked index of value is send to particular JSpinner.
 *
 * @author Martin Podhora
 */
public class HightlightingChartMouseListener implements ChartMouseListener {
    private final ChartPanel chartPanel;
    private final JSpinner firstTimeJSpinner;
    private final JSpinner lastTimeJSpinner;
    private final ExtractionTabModel extractionTabModel;

    public HightlightingChartMouseListener(ChartPanel chartPanel, JSpinner firstTimeJSpinner, JSpinner lastTimeJSpinner, ExtractionTabModel extractionTabModel) {
        this.chartPanel = chartPanel;
        this.firstTimeJSpinner = firstTimeJSpinner;
        this.lastTimeJSpinner = lastTimeJSpinner;
        this.extractionTabModel = extractionTabModel;
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent event)
    {
        ChartEntity entity = event.getEntity();

        if (!isDataEntity(entity))
        {
            double x = ((event.getTrigger().getX() - chartPanel.getInsets().left) / chartPanel.getScaleX());
            double minDistance = Integer.MAX_VALUE;

            Collection entities = chartPanel.getChartRenderingInfo().getEntityCollection().getEntities();
            for (Iterator iter = entities.iterator(); iter.hasNext();)
            {
                ChartEntity element = (ChartEntity) iter.next();

                if (isDataEntity(element))
                {
                    Rectangle rect = element.getArea().getBounds();
                    double centerPointX = rect.getCenterX();
                    double dist = Math.abs(x - centerPointX);
                    if (dist < minDistance)
                    {
                        minDistance = dist;
                        entity = element;
                    }
                }
            }
        }

        if (entity != null)
        {
            handleChartMouseClicked((XYItemEntity)entity);
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent cme) {
    }

    protected boolean isDataEntity(ChartEntity entity)
    {
        return (entity instanceof XYItemEntity);
    }

    private int getIndexOfClickedValue(double realx) {
        XYPlot plot = chartPanel.getChart().getXYPlot();

        double oldDifference = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < plot.getDataset().getItemCount(0); i++) {
            double difference = Math.max(realx, plot.getDataset().getXValue(0, i)) - Math.min(realx, plot.getDataset().getXValue(0, i));
            if (difference > oldDifference) {
                index = i - 1;
                break;
            }
            oldDifference = difference;
        }

        return index;
    }

    private double getXClickedOnChart(ChartMouseEvent cme) {
        cme.getChart().getXYPlot().getDataset();
        XYItemEntity xyitem = (XYItemEntity)cme.getEntity();
        XYDataset ds = xyitem.getDataset();
        return ds.getXValue(xyitem.getSeriesIndex(), xyitem.getItem());
    }

    public void handleChartMouseClicked(XYItemEntity entity) {
        int clickedIndexOnModifiedTrace = entity.getItem();
        double[] time = extractionTabModel.getCurrentTrace().getTime(false);

        if (extractionTabModel.getFirstIndexOnChartTrace() == null || extractionTabModel.getFirstIndexOnChartTrace() == 0) {
            extractionTabModel.setFirstIndexOnChartTrace(clickedIndexOnModifiedTrace);
            firstTimeJSpinner.setValue(time[extractionTabModel.getFirstIndexOnChartTrace()]);
        } else {
            if (clickedIndexOnModifiedTrace < extractionTabModel.getFirstIndexOnChartTrace()) {
                extractionTabModel.setLastIndexOnChartTrace(extractionTabModel.getFirstIndexOnChartTrace());
                extractionTabModel.setFirstIndexOnChartTrace(clickedIndexOnModifiedTrace);
            } else {
                extractionTabModel.setLastIndexOnChartTrace(clickedIndexOnModifiedTrace);
            }

            firstTimeJSpinner.setValue(time[extractionTabModel.getFirstIndexOnChartTrace()]);
            lastTimeJSpinner.setValue(time[extractionTabModel.getLastIndexOnChartTrace()]);
        }
    }
}
