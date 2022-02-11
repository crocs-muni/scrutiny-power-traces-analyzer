package muni.scrutiny.module.configurations.input;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class TCIOperation {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TCIOperation";

    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("file_paths")
    public List<String> filePaths;
}
