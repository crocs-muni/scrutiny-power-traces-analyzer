package muni.scrutiny.module.pipelines.base;

import com.google.gson.annotations.SerializedName;

public class CustomPipelineParameter {
    @SerializedName("parameter")
    private String parameter;

    @SerializedName("value")
    private String value;

    public String getParameter() {
        return parameter;
    }

    public String getValue() {
        return value;
    }
}
