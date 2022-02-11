package muni.scrutiny.module.configurations.module;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.configurations.output.TracesComparerOutput;

public class TracesComparerModule {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TracesComparerModule";

    @SerializedName("module_name")
    public String moduleName = "TRACES_COMPARER";

    @SerializedName("module_data")
    public TracesComparerOutput moduleData;
}
