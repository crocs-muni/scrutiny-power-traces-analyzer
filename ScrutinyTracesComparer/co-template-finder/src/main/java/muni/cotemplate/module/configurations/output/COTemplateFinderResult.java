package muni.cotemplate.module.configurations.output;

import com.google.gson.annotations.SerializedName;
import muni.cotemplate.module.configurations.input.COTemplateConfiguration;

import java.util.List;

public class COTemplateFinderResult {
    @SerializedName("ideal_operation_length_time")
    public double operationLengthTime;

    @SerializedName("ideal_operation_length")
    public int operationLength;

    @SerializedName("real_trace_path")
    public String realTracePath;

    @SerializedName("operation_template_path")
    public String operationTemplatePath;

    @SerializedName("template_image_path")
    public String templateImagePath;

    @SerializedName("partial_results")
    public List<COTemplateFinderWidthResult> partialResults;

    @SerializedName("used_config")
    public COTemplateConfiguration usedConfig;
}
