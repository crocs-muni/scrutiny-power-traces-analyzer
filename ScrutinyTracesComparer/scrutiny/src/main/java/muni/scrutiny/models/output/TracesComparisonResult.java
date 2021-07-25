package muni.scrutiny.models.output;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TracesComparisonResult {
    @SerializedName("metric")
    public String metric;

    @SerializedName("tracesresults")
    public List<TraceComparisonResult> tracesResults = new ArrayList<TraceComparisonResult>();
}
