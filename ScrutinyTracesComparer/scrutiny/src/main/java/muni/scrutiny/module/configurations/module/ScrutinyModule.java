package muni.scrutiny.module.configurations.module;

import com.google.gson.annotations.SerializedName;

public class ScrutinyModule<T> {
    @SerializedName("module_name")
    public String moduleName = "Traces comparer SCRUTINY module";

    @SerializedName("data")
    public T data;
}
