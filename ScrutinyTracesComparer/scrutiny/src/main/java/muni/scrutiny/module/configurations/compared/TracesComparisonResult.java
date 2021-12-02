package muni.scrutiny.module.configurations.compared;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.configurations.compared.TraceComparisonResult;

import java.util.List;

public class TracesComparisonResult {
    @SerializedName("traces_results")
    public List<TraceComparisonResult> tracesResults;
}
