package muni.scrutiny.testinggui.models;

public class MainFormModel implements IModel {
    private ExtractionTabModel extractionTabModel;
    private VisualizationTabModel visualizationTabModel;

    public MainFormModel() {
        this.extractionTabModel = new ExtractionTabModel();
        this.visualizationTabModel = new VisualizationTabModel();
    }

    public ExtractionTabModel getExtractionTabModel() {
        return extractionTabModel;
    }

    public VisualizationTabModel getVisualizationTabModel() { return visualizationTabModel; }

    @Override
    public void clear() {
        extractionTabModel.clear();
        visualizationTabModel.clear();
        System.gc();
    }
}
