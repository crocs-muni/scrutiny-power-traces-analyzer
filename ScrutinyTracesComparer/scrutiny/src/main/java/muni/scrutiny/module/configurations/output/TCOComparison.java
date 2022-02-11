package muni.scrutiny.module.configurations.output;

import com.google.gson.annotations.SerializedName;

public class TCOComparison {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TCOComparison";

    @SerializedName("distance")
    public double distance;

    @SerializedName("file_path")
    public String file_path;
}
