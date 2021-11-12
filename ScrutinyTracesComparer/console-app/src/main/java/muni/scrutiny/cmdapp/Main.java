package muni.scrutiny.cmdapp;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        String unknownConfigPath = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\Unknown\\config.json";
//        String ucDirectory = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\Unknown";
//        String unknownJson = readFile(unknownConfigPath, StandardCharsets.UTF_8);
//        String referenceConfigPath = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\NXP Reference\\reference-config.json";
//        String rcDirectory = "C:\\Users\\marti\\Desktop\\School\\SDIPR\\NXP JCOP 31 v2.4.1\\Extracted operations\\NXP Reference";
//        String referenceJson = readFile(referenceConfigPath, StandardCharsets.UTF_8);
//
//        TracesComparisonResult tcr = new TracesComparisonResult();
//        tcr.metric = "Test";
//        for (ReferenceCardConfigTrace rct : referenceCardConfig.traces) {
//            Optional<ComparedCardConfigTrace> ct = newCardConfig.traces.stream()
//                    .filter((tr) -> tr.code.equals(rct.code))
//                    .findFirst();
//            if (!ct.isPresent()) {
//                tcr.tracesResults.add(new TraceComparisonResult(rct.code));
//                continue;
//            }
//
//            String uknownTracePath = ucDirectory + "\\" + ct.get().filepath;
//            String referenceTracePath = rcDirectory + "\\" + rct.filepath;
//            Trace unknownTrace = DataManager.loadTrace(Paths.get(uknownTracePath), false);
//            Trace referenceTrace = DataManager.loadTrace(Paths.get(referenceTracePath), false);
//            DistanceMeasure dm = new EuclideanDistance();
//            double[] utv = unknownTrace.getVoltage();
//            double[] rtv = referenceTrace.getVoltage();
//            tcr.tracesResults.add(new TraceComparisonResult(
//                    rct.code,
//                    dm.compute(utv.length > rtv.length ? rtv : utv, utv.length > rtv.length ? utv : rtv, 0)));
//        }
//
//        String tcrJson = new Gson().toJson(tcr);
//        String outputFilePath = ucDirectory + "\\" + "result.json";
//        try (PrintWriter out = new PrintWriter(outputFilePath)) {
//            out.println(tcrJson);
//        }
    }
}
