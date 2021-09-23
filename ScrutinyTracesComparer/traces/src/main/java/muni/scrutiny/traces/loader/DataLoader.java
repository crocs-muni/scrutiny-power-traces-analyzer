package muni.scrutiny.traces.loader;

import com.opencsv.CSVReader;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Class contains static helper methods for data loading from .csv files.
 * 
 * @author Martin Podhora
 */
public class DataLoader {
    private static int countLinesInCsv(Path filePath) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        }
    }

    public static Trace importFromCsv(
            Path filePath,
            int timeColumn,
            int voltageColumn,
            boolean notSkipping)
            throws FileNotFoundException, IOException {
        String[] csvRow;
        int linesInCsv = countLinesInCsv(filePath) - DataManager.DATA_STARTING_LINE;
        int sizeNeededForArray = linesInCsv;
        int addDataPositionIndex = 0;
        String timeUnit;
        String voltageUnit;
        double timeValue;
        double voltageValue;
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath.toFile()))) {
            try (CSVReader csvReader = new CSVReader(bufferedReader)) {
                csvRow = csvReader.readNext();
                if (csvRow.length < timeColumn && csvRow.length < voltageColumn) {
                    throw new IOException("Invalid format of CSV file.");
                }

                csvRow = csvReader.readNext();
                timeUnit = csvRow[timeColumn];
                voltageUnit = csvRow[voltageColumn];
                csvReader.skip(1);

                Trace trace = new Trace(filePath.getFileName().toString(), sizeNeededForArray, voltageUnit, timeUnit);

                while ((csvRow = csvReader.readNext()) != null && addDataPositionIndex < sizeNeededForArray) {
                    timeValue = Double.parseDouble(csvRow[timeColumn]);
                    voltageValue = Double.parseDouble(csvRow[voltageColumn]);
                    trace.addData(voltageValue, timeValue, addDataPositionIndex);
                    addDataPositionIndex++;
                }
                return trace;    
            }
        }
    }
}
