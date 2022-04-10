package muni.scrutiny.dbclassifier.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OperationDBCResult {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.traceclassifier.OperationDBCResult";

    @SerializedName("operation_code")
    public String operationCode;

    @SerializedName("similarity_intervals")
    public List<SimilarityInterval> similarityIntervals;

    @SerializedName("visualized_operations")
    public String visualizedOperations;
}
