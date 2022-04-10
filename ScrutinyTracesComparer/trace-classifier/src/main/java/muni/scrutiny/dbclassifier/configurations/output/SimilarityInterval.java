package muni.scrutiny.dbclassifier.configurations.output;

import com.google.gson.annotations.SerializedName;

public class SimilarityInterval {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.traceclassifier.SimilarityInterval";

    @SerializedName("similarity_value")
    public double similarityValue;

    @SerializedName("similarity_value_type")
    public String similarityValueType;

    @SerializedName("time_from")
    public double timeFrom;

    @SerializedName("time_to")
    public double timeTo;

    @SerializedName("indexes_compared")
    public int indexesCompared;
}
