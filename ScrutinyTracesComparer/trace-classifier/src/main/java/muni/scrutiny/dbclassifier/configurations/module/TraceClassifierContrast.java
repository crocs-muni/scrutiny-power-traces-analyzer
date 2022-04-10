package muni.scrutiny.dbclassifier.configurations.module;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.dbclassifier.configurations.output.CardDBCResult;

import java.util.List;

public class TraceClassifierContrast {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.traceclassifier.TraceClassifierContrast";

    @SerializedName("module_name")
    public String moduleName = "Trace Classifier";

    @SerializedName("result")
    public String result;

    @SerializedName("results")
    public List<CardDBCResult> results;
}
