package muni.scrutiny.module.configurations.reference;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReferenceCardOperationExecTime {
    public ReferenceCardOperationExecTime() {
    }

    public ReferenceCardOperationExecTime(String unit, Double time) {
        this.unit = unit;
        this.time = time;
    }

    @SerializedName("unit")
    public String unit;

    @SerializedName("time")
    public Double time;
}
