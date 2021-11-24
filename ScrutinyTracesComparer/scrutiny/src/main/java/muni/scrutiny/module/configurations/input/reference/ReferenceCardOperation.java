package muni.scrutiny.module.configurations.input.reference;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class ReferenceCardOperation {
    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("execution_times")
    public List<ReferenceCardOperationExecTime> executionTimes;

    @SerializedName("measurements")
    public List<ReferenceMeasurements> measurements;

    @SerializedName("file_paths")
    public List<String> filePaths;
}
