package muni.scrutiny.module.configurations.input.reference;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class ReferenceCardConfigTrace {
    @SerializedName("primary_code_name")
    public String primaryCodeName;

    @SerializedName("alternative_code_names")
    public List<String> alternativeCodeNames;

    @SerializedName("parameters")
    public CustomPipelineParameters parameters;

    @SerializedName("file_name")
    public String fileName;

    public String getCorrectFileName() {
        return fileName != null ? fileName : primaryCodeName + ".csv";
    }
}
