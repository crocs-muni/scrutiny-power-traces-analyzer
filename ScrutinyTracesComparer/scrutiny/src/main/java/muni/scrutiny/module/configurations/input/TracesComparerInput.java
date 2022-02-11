package muni.scrutiny.module.configurations.input;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class TracesComparerInput {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TracesComparerInput";

    @SerializedName("card_code")
    public String cardCode;

    @SerializedName("pipelines")
    public List<String> pipelines;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("operations")
    public List<TCIOperation> operations;

    @SerializedName("created_by")
    public String createdBy;

    @SerializedName("additional_info")
    public String additionalInfo;
}
