package muni.scrutiny.testinggui.models;

public class MainFormModel implements IModel {
    private ExtractionTabModel extractionTabModel;

    public MainFormModel() {
        this.extractionTabModel = new ExtractionTabModel();
    }

    public ExtractionTabModel getExtractionTabModel() {
        return extractionTabModel;
    }

    @Override
    public void clear() {
        extractionTabModel.clear();
        System.gc();
    }
}
