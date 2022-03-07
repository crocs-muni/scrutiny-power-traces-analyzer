package muni.scrutiny.tracesconcat.configurations;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TracesConcatConfig {
    @SerializedName("trace_paths")
    public List<String> tracePaths;
}
