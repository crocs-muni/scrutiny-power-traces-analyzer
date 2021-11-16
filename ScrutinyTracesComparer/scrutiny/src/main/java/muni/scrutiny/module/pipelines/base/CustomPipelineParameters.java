package muni.scrutiny.module.pipelines.base;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Optional;

public class CustomPipelineParameters {
    @SerializedName("parameters")
    private List<CustomPipelineParameter> parameters;

    public Double getDoubleParameter(String parameter) {
        Optional<CustomPipelineParameter> value = parameters.stream().filter(p -> p.equals(parameter)).findFirst();
        if (value.isPresent()) {
            return Double.parseDouble(value.get().getValue());
        }

        return null;
    }
}
