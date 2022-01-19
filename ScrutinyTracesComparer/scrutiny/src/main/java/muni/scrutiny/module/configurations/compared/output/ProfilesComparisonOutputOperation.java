package muni.scrutiny.module.configurations.compared.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfilesComparisonOutputOperation {
    public ProfilesComparisonOutputOperation() {
    }

    public ProfilesComparisonOutputOperation(String operationCode, List<Double> comparisonResults) {
        this.operationCode = operationCode;
        this.comparisonResults = comparisonResults;
        this.operationPresent = true;
    }

    public ProfilesComparisonOutputOperation(String operationCode) {
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
