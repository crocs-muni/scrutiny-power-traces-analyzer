package muni.scrutiny;

import com.google.gson.Gson;
import muni.scrutiny.measures.DistanceMeasure;
import muni.scrutiny.measures.EuclideanDistance;
import muni.scrutiny.models.Trace;
import muni.scrutiny.models.input.compared.ComparedCardConfig;
import muni.scrutiny.models.input.compared.ComparedCardConfigTrace;
import muni.scrutiny.models.input.reference.ReferenceCardConfig;
import muni.scrutiny.models.input.reference.ReferenceCardConfigTrace;
import muni.scrutiny.models.output.TraceComparisonResult;
import muni.scrutiny.models.output.TracesComparisonResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException {
        String unknownConfigPath = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\Unknown\\config.json";
        String ucDirectory = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\Unknown";
        String unknownJson = readFile(unknownConfigPath, StandardCharsets.UTF_8);
        String referenceConfigPath = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\NXP Reference\\reference-config.json";
        String rcDirectory = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\NXP Reference";
        String referenceJson = readFile(referenceConfigPath, StandardCharsets.UTF_8);

        ComparedCardConfig comparedCardConfig = new Gson().fromJson(unknownJson, ComparedCardConfig.class);
        ReferenceCardConfig referenceCardConfig = new Gson().fromJson(referenceJson, ReferenceCardConfig.class);

        TracesComparisonResult tcr = new TracesComparisonResult();
        tcr.metric = "Test";
        for (ReferenceCardConfigTrace rct : referenceCardConfig.traces) {
            Optional<ComparedCardConfigTrace> ct = comparedCardConfig.traces.stream()
                    .filter((tr) -> tr.code.equals(rct.code))
                    .findFirst();
            if (!ct.isPresent()) {
                tcr.tracesResults.add(new TraceComparisonResult(rct.code));
                continue;
            }

            String uknownTracePath = ucDirectory + "\\" + ct.get().filepath;
            String referenceTracePath = rcDirectory + "\\" + rct.filepath;
            Trace unknownTrace = DataManager.loadTrace(uknownTracePath, true);
            Trace referenceTrace = DataManager.loadTrace(referenceTracePath, true);
            DistanceMeasure dm = new EuclideanDistance();
            double[] utv = unknownTrace.getVoltage();
            double[] rtv = referenceTrace.getVoltage();
            tcr.tracesResults.add(new TraceComparisonResult(
                    rct.code,
                    dm.compute(utv.length > rtv.length ? rtv : utv, utv.length > rtv.length ? utv : rtv, 0)));
        }

        String tcrJson = new Gson().toJson(tcr);
        String outputFilePath = ucDirectory + "\\" + "result.json";
        try (PrintWriter out = new PrintWriter(outputFilePath)) {
            out.println(tcrJson);
        }
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
