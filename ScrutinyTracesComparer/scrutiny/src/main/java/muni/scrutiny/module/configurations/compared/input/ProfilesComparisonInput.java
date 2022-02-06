package muni.scrutiny.module.configurations.compared.input;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfilesComparisonInput {
    @SerializedName("operations")
    public List<ProfilesComparisonInputOperation> operations;
}
