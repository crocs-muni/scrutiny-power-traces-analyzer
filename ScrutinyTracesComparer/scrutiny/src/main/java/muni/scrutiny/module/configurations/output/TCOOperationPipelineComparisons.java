package muni.scrutiny.module.configurations.output;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;

import java.util.List;
import java.util.stream.Collectors;

public class TCOOperationPipelineComparisons {
    @SerializedName("py/object")
    public String pyObject = "scrutiny.javacard.modules.tracescomparer.TCOOperationPipelineComparisons";

    @SerializedName("pipeline")
    public String pipeline;

    @SerializedName("metric_type")
    public String metricType;

    @SerializedName("comparisons")
    public List<TCOComparison> comparisons;

    public double getConfidenceIntervalLowerBound(double p) {
        TDistribution tdist = new TDistribution(comparisons.size() - 1);
        double prob = tdist.inverseCumulativeProbability(p);
        double[] data = comparisons.stream().mapToDouble(c -> c.distance).toArray();
        double sigma = Math.sqrt(StatUtils.variance(data));
        double mean = StatUtils.mean(data);
        return mean - (sigma/Math.sqrt(comparisons.size()))*prob;
    }

    public double getConfidenceIntervalUpperBound(double p) {
        TDistribution tdist = new TDistribution(comparisons.size() - 1);
        double prob = tdist.inverseCumulativeProbability(p);
        double[] data = comparisons.stream().mapToDouble(c -> c.distance).toArray();
        double sigma = Math.sqrt(StatUtils.variance(data));
        double mean = StatUtils.mean(data);
        return mean + (sigma/Math.sqrt(comparisons.size()))*prob;
    }
}
