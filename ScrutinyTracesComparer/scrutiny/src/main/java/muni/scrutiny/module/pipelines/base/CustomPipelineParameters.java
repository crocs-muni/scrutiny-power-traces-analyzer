package muni.scrutiny.module.pipelines.base;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Optional;

public class CustomPipelineParameters {
    @SerializedName("parameters")
    public List<CustomPipelineParameter> parameters;

    public boolean areNullOrEmpty() {
        return parameters == null || parameters.size() == 0;
    }

    public Double getDoubleParameter(String parameter) {
        Optional<CustomPipelineParameter> value = parameters.stream().filter(p -> p.equals(parameter)).findFirst();
        if (value.isPresent()) {
            return Double.parseDouble(value.get().getValue());
        }

        return null;
    }
}
