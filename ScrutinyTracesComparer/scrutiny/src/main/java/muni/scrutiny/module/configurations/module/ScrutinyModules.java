package muni.scrutiny.module.configurations.module;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ScrutinyModules {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.device.Device";

    @SerializedName("device_type")
    public String deviceType = "No device";

    @SerializedName("name")
    public String name = "Traces comparer SCRUTINY modules";

    @SerializedName("modules")
    public Map<String, ScrutinyModule> modules = new HashMap<>();
}
