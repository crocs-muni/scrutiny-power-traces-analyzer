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

    @SerializedName("pipeline_comparisons")
    public List<TCOOperationPipelineComparisons> pipelineComparisonResults;

    @SerializedName("operation_traces_paths")
    public List<String> operationTracesPaths;

    @SerializedName("execution_times")
    public List<TCOOperationExecTime> executionTimes;

    @SerializedName("operation_present")
    public boolean operationPresent;

    public double getConfidenceIntervalLowerBound(String pipeline, double p) {
        TCOOperationPipelineComparisons tcoopc = pipelineComparisonResults
                .stream()
                .filter(cr -> cr.metricType.equalsIgnoreCase(pipeline))
                .findFirst()
                .orElse(null);
        return tcoopc != null ? tcoopc.getConfidenceIntervalLowerBound(p) : 0;
    }

    public double getConfidenceIntervalUpperBound(String pipeline, double p) {
        TCOOperationPipelineComparisons tcoopc = pipelineComparisonResults
                .stream()
                .filter(cr -> cr.pipeline.equalsIgnoreCase(pipeline))
                .findFirst()
                .orElse(null);
        return tcoopc != null ? tcoopc.getConfidenceIntervalUpperBound(p) : 0;
    }
}
