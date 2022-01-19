package muni.scrutiny.module.configurations.compared.input;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfilesComparisonInputOperation {
    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("file_paths")
    public List<String> filePaths;
}
