package muni.scrutiny.cmdapp.actions.utils;

import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {
    public static String readFile(Path path) throws ActionException {
        try {
            byte[] encoded = Files.readAllBytes(path);
            return new String(encoded);
        } catch (IOException exception) {
            throw new ActionException(exception);
        }
    }

    public static List<File> getFilesInDirectory(Path directory) throws ActionException {
        List<File> files = Arrays.stream(directory.toFile().listFiles())
                .filter(f -> f.isFile())
                .collect(Collectors.toList());
        return files;
    }

    public static List<Path> getPathsToOperationTraces(List<File> allFiles, List<String> operationPaths, String operationCode) {
        List<Path> pathsToOperations;
        if (operationPaths != null && operationPaths.size() > 0) {
            pathsToOperations = allFiles
                    .stream()
                    .filter(f -> operationPaths.stream().anyMatch(fp -> f.getAbsolutePath().contains(fp)))
                    .map(f -> f.toPath())
                    .collect(Collectors.toList());
        } else {
            pathsToOperations = allFiles
                    .stream()
                    .filter(f -> f.getName().contains(operationCode))
                    .map(f -> f.toPath())
                    .collect(Collectors.toList());
        }

        return pathsToOperations;
    }

    public static List<Trace> getOperationTraces(List<Path> pathsToOperations) throws IOException {
        List<Trace> operationTrace = new ArrayList<>();
        for (Path path : pathsToOperations) {
            operationTrace.add(DataManager.loadTrace(path, false));
        }

        return operationTrace;
    }

    public static String saveComparisonImage(Path outputPath, JFreeChart chart) throws IOException {
        String imageName = chart.getTitle().getText() + ".png";
        File outputfile = outputPath.resolve(imageName).toFile();
        OutputStream out = new FileOutputStream(outputfile);
        ChartUtilities.writeChartAsPNG(out, chart,800,600);
        return imageName;
    }
}
