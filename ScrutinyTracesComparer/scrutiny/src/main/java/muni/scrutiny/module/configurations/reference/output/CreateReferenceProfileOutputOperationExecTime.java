package muni.scrutiny.module.configurations.reference.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateReferenceProfileOutputOperationExecTime {
    public CreateReferenceProfileOutputOperationExecTime() {
    }

    public CreateReferenceProfileOutputOperationExecTime(String unit, Double time) {
        this.unit = unit;
        this.time = time;
    }

    @SerializedName("unit")
    public String unit;

    @SerializedName("time")
    public Double time;
}
