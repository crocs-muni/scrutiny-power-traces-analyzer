package muni.scrutiny.dbclassifier.configurations.module;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SCRUTINYContrast {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.interfaces.Contrast";

    @SerializedName("ref_name")
    public String refName;

    @SerializedName("prof_name")
    public String profName = "Trace to classify";

    @SerializedName("result")
    public String result = "ContrastState.MATCH";

    @SerializedName("contrasts")
    public List<TraceClassifierContrast> contrasts;
}
