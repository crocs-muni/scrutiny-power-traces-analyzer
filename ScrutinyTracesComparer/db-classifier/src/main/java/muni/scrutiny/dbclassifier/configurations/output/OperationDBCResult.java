package muni.scrutiny.dbclassifier.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OperationDBCResult {
    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("starting_times")
    public List<Double> startingTimes;

    @SerializedName("distances")
    public List<Double> distances;

    @SerializedName("visualized_operations")
    public String visualizedOperations;
}
