package muni.scrutiny.configurations.input.compared;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ComparedCardConfig {
    @SerializedName("traces")
    public List<ComparedCardConfigTrace> traces;
}
