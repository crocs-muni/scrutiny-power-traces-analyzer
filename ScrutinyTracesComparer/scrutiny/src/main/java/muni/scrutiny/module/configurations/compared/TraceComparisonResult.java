package muni.scrutiny.module.configurations.compared;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TraceComparisonResult {
    public TraceComparisonResult() {
    }

    public TraceComparisonResult(String operationCode, List<Double> comparisonResults) {
        this.operationCode = operationCode;
        this.comparisonResults = comparisonResults;
        this.operationPresent = true;
    }

    public TraceComparisonResult(String operationCode) {
        this.operationCode = operationCode;
        this.operationPresent = false;
    }

    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("comparison_results")
    public List<Double> comparisonResults;

    @SerializedName("charts")
    public List<String> charts;

    @SerializedName("operation_present")
    public boolean operationPresent;
}
