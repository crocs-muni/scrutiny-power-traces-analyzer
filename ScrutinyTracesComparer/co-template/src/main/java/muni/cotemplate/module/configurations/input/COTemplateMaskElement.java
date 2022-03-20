package muni.cotemplate.module.configurations.input;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class COTemplateMaskElement {
    @SerializedName("mask_element")
    public Character maskElement;

    @SerializedName("times")
    public List<Double> times;
}
