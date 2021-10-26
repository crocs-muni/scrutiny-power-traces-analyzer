package muni.scrutiny.testinggui.models.resources;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ComboItemModel {
    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    public ComboItemModel() {
    }

    public ComboItemModel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() { return value; }
}
