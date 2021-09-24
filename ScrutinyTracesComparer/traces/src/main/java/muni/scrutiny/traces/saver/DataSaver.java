package muni.scrutiny.traces.saver;

import com.opencsv.CSVWriter;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Class contains static helper methods for data saving to .csv files.
 * 
 * @author Martin Podhora
 */
public class DataSaver {
    public static void exportToCsv(Trace trace, Path dataPath) throws IOException {
        privateExportToCsv(trace, dataPath, 0, trace.getDataCount() - DataManager.DATA_STARTING_LINE);
    }

    public static void exportToCsv(Trace trace, Path dataPath, int firstIndex, int lastIndex) throws IOException {
        privateExportToCsv(trace, dataPath, firstIndex, lastIndex);
    }

    private static void privateExportToCsv(Trace trace, Path dataPath, int firstIndex, int lastIndex) throws IOException {
        String[] csvRow = new String[2];
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dataPath.toFile()))) {
            try (CSVWriter csvWriter = new CSVWriter(bw)) {
                csvRow[0] = "Time";
                csvRow[1] = "Voltage";
                csvWriter.writeNext(csvRow);
                csvRow[0] = trace.getTimeUnit();
                csvRow[1] = trace.getVoltageUnit();
                csvWriter.writeNext(csvRow);
                csvWriter.writeNext(new String[2]);
                for (int i = firstIndex; i < lastIndex; i++) {
                    csvRow[0] = String.valueOf(trace.getTimeOnPosition(i));
                    csvRow[1] = String.valueOf(trace.getVoltageOnPosition(i));
                    csvWriter.writeNext(csvRow);
                }
            }
        }
    }
}
