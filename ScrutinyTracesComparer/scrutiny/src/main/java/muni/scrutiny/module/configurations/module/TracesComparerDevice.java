package muni.scrutiny.module.configurations.module;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class TracesComparerDevice {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TraceComparerDevice";

    @SerializedName("device_type")
    public String deviceType = "SMART_CARD";

    @SerializedName("name")
    public String name;

    @SerializedName("modules")
    public Map<String, TracesComparerModule> modules = new HashMap<>();
}
