package muni.scrutiny.module.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TCOOperationPipelineComparisons {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TCOOperationPipelineComparisons";

    @SerializedName("pipeline")
    public String pipeline;

    @SerializedName("comparisons")
    public List<TCOComparison> comparisons;
}
