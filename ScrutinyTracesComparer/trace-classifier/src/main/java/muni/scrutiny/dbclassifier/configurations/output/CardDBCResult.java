package muni.scrutiny.dbclassifier.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardDBCResult {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.traceclassifier.CardDBCResult";

    @SerializedName("card_code")
    public String cardCode;

    @SerializedName("operations_results")
    public List<OperationDBCResult> operationResults;
}
