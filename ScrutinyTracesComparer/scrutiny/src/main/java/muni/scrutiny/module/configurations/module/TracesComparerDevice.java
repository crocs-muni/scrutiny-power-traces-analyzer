package muni.scrutiny.module.configurations.module;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class TracesComparerDevice {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TraceComparerDevice";

    @SerializedName("device_type")
    public String deviceType = "";

    @SerializedName("name")
    public String name = "TRACES_COMPARER";

    @SerializedName("modules")
    public Map<String, TracesComparerModule> modules = new HashMap<>();
}
