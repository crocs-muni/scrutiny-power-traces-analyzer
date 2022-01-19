package muni.scrutiny.module.configurations.reference.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateReferenceProfileOutputMeasurements {
    public CreateReferenceProfileOutputMeasurements() {
        pipeline = null;
        distances = null;
    }

    public CreateReferenceProfileOutputMeasurements(String pipeline, List<Double> distances) {
        this.pipeline = pipeline;
        this.distances = distances;
    }

    @SerializedName("pipeline")
    public String pipeline;

    @SerializedName("distances")
    public List<Double> distances;
}
