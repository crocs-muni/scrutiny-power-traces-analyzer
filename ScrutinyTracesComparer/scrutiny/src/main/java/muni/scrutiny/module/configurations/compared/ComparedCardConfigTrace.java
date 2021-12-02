package muni.scrutiny.module.configurations.compared;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ComparedCardConfigTrace {
    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("file_paths")
    public List<String> filePaths;
}
