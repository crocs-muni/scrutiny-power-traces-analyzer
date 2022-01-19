package muni.scrutiny.module.configurations.reference.output;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class CreateReferenceProfileOutputOperation {
    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("execution_times")
    public List<CreateReferenceProfileOutputOperationExecTime> executionTimes;

    @SerializedName("measurements")
    public List<CreateReferenceProfileOutputMeasurements> measurements;

    @SerializedName("file_paths")
    public List<String> filePaths;
}
