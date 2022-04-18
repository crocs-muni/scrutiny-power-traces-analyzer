package muni.cotemplate.module.configurations.output;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class COTemplatePeaksResult {
    @SerializedName("is_match")
    public boolean isMatch;

    @SerializedName("expected_n")
    public int expectedN;

    @SerializedName("real_n")
    public int realN;

    @SerializedName("all_candidates")
    public List<Double> allCandidates;

    @SerializedName("chosen_candidates")
    public List<Double> chosenCandidates;

    @SerializedName("confidence_coefficient")
    public double confidenceCoefficient;

    @SerializedName("confidence_interval_lower_bound")
    public double confidenceIntervalLowerBound;

    @SerializedName("starting_times")
    public List<Double> startingTimes;

    @SerializedName("n_visualizations_image_path")
    public String nVisualizationImagePath;

    @SerializedName("cotemp_correlation_path")
    public String coTempCorrelationPath;

    @SerializedName("cotemp_correlation_image_path")
    public String coptempCorrelationImagePath;
}
