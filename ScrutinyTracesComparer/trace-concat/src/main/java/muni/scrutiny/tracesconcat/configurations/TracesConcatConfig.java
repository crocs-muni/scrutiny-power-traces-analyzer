package muni.scrutiny.tracesconcat.configurations;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TracesConcatConfig {
    @SerializedName("offset_coeff")
    public Double offsetCoeff;

    @SerializedName("trace_paths")
    public List<String> tracePaths;

    public double getOffsetCoeff() {
        return offsetCoeff == null ? 0.05 : offsetCoeff;
    }
}
