package muni.scrutiny.models.input.reference;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReferenceCardConfig {
    @SerializedName("traces")
    public List<ReferenceCardConfigTrace> traces;
}
