package muni.scrutiny.dbclassifier.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DBClassifierOutput {
    @SerializedName("most_similar_card")
    public String mostSimilarCard;

    @SerializedName("card_results")
    public List<CardDBCResult> cardResults;
}
