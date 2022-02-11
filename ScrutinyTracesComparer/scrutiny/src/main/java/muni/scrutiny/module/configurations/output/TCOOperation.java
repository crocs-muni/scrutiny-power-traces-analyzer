package muni.scrutiny.module.configurations.output;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class TCOOperation {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TCOOperation";

    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("comparisons")
    public List<TCOOperationPipelineComparisons> comparisonResults;

    @SerializedName("operation_traces_paths")
    public List<String> operationTracesPaths;

    @SerializedName("execution_times")
    public List<TCOOperationExecTime> executionTimes;

    @SerializedName("operation_present")
    public boolean operationPresent;
}
