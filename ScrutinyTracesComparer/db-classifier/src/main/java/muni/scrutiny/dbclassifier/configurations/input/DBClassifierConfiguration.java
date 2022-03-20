package muni.scrutiny.dbclassifier.configurations.input;

import com.google.gson.annotations.SerializedName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DBClassifierConfiguration {
    @SerializedName("reference_profiles_paths")
    public List<String> referenceProfilesPaths;

    public List<Path> getPaths() {
        return referenceProfilesPaths.stream().map(p -> Paths.get(p)).collect(Collectors.toList());
    }
}
