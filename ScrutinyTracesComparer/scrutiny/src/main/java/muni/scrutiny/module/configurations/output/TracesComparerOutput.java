package muni.scrutiny.module.configurations.output;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.time.LocalDateTime;
import java.util.List;

public class TracesComparerOutput {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TracesComparerOutput";

    @SerializedName("card_code")
    public String cardCode;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("results")
    public List<TCOOperation> results;

    @SerializedName("created_date")
    public String createdDate;

    @SerializedName("created_by")
    public String createdBy;

    @SerializedName("additional_info")
    public String additionalInfo;
}
