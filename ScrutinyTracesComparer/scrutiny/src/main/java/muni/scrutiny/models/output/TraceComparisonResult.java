package muni.scrutiny.models.output;

import com.google.gson.annotations.SerializedName;

public class TraceComparisonResult {
    public TraceComparisonResult(String operationCode, double comparisonResult) {
        this.operationCode = operationCode;
        this.comparisonResult = comparisonResult;
        this.operationPresent = true;
    }

    public TraceComparisonResult(String operationCode) {
        this.operationCode = operationCode;
        this.operationPresent = false;
    }

    @SerializedName("operationcode")
    public String operationCode;

    @SerializedName("comparisonresult")
    public double comparisonResult;

    @SerializedName("operationpresent")
    public boolean operationPresent;
}
