package muni.scrutiny.module.configurations.compared.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfilesComparisonOutput {
    @SerializedName("traces_results")
    public List<ProfilesComparisonOutputOperation> tracesResults;
}
