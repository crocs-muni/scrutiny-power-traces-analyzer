package muni.scrutiny.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TracesComparisonResult {
    @SerializedName("metric")
    public String metric;

    @SerializedName("traces_results")
    public List<TraceComparisonResult> tracesResults = new ArrayList<TraceComparisonResult>();
}
