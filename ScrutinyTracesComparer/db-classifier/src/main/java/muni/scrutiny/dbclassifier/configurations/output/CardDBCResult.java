package muni.scrutiny.dbclassifier.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardDBCResult {
    @SerializedName("cardname")
    public String cardCode;

    @SerializedName("operations_results")
    public List<OperationDBCResult> operationResults;
}
