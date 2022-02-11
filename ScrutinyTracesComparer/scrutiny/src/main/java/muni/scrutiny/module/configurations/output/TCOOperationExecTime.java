package muni.scrutiny.module.configurations.output;

import com.google.gson.annotations.SerializedName;

public class TCOOperationExecTime {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TCOOperationExecTime";

    @SerializedName("unit")
    public String unit;

    @SerializedName("time")
    public Double time;
}
