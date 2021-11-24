package muni.scrutiny.module.configurations.input.reference;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReferenceMeasurements {
    public ReferenceMeasurements() {
        pipeline = null;
        distances = null;
    }

    public ReferenceMeasurements(String pipeline, List<Double> distances) {
        this.pipeline = pipeline;
        this.distances = distances;
    }

    @SerializedName("pipeline")
    public String pipeline;

    @SerializedName("distances")
    public List<Double> distances;
}
