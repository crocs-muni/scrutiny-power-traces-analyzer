package muni.cotemplate.module.configurations;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class COTemplateConfiguration {
    @SerializedName("mask")
    public String mask;

    @SerializedName("mask_elements")
    public List<COTemplateMaskElement> elements;
}
