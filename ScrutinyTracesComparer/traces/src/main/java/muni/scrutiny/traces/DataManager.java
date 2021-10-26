package muni.scrutiny.traces;

import muni.scrutiny.traces.loader.DataLoader;
import muni.scrutiny.traces.models.Trace;
import muni.scrutiny.traces.saver.DataSaver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This class contains many utility methods used for data loading and saving.
 * HashMap will in future be replaced with resource file.
 * 
 * @author Martin Podhora
 */
public class DataManager {
    public static final int DEFAULT_TIME_COLUMN = 0;
    public static final int DEFAULT_VOLTAGE_COLUMN = 1;
    public static final int DATA_STARTING_LINE = 3;

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        else {
            return "";
        }
    }

    public static Trace loadTrace(Path filePath, boolean loadTime) throws IOException {
        return DataLoader.importFromCsv(filePath, DEFAULT_TIME_COLUMN, DEFAULT_VOLTAGE_COLUMN, loadTime);
    }

    public static void saveTrace(Path filePath, Trace trace) throws IOException {
        DataSaver.exportToCsv(trace, filePath);
    }
    
    public static void saveTrace(Path filePath, Trace trace, int firstIndex, int lastIndex) throws IOException {
        DataSaver.exportToCsv(trace, filePath, firstIndex, lastIndex);
    }
}
