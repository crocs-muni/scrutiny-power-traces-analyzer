package muni.scrutiny.testinggui.models.resources;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VisualizationResourcesModel {
    @SerializedName("pipelines")
    private List<ComboItemModel> pipelines;
}
