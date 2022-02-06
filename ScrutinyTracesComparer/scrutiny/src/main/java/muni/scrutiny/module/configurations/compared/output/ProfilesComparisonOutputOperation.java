package muni.scrutiny.module.configurations.compared.output;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.configurations.reference.output.CreateReferenceProfileOutputOperationExecTime;

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

    @SerializedName("pipeline_code")
    public String pipelineCode;

    @SerializedName("comparison_results")
    public List<Double> comparisonResults;

    @SerializedName("execution_times")
    public List<CreateReferenceProfileOutputOperationExecTime> executionTimes;

    @SerializedName("charts")
    public List<String> charts;

    @SerializedName("operation_present")
    public boolean operationPresent;
}
