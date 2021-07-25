package muni.scrutiny;

import muni.scrutiny.loader.DataLoader;
import muni.scrutiny.models.Trace;
import muni.scrutiny.saver.DataSaver;

import java.io.File;
import java.io.IOException;

/**
 * This class contains many utility methods used for data loading and saving.
 * HashMap will in future be replaced with resource file.
 * 
 * @author Martin Podhora
 */
public class DataManager {
    /**
     * Column of time values in .csv file.
     */
    public static final int DEFAULT_TIME_COLUMN = 0;
    
    /**
     * Column of voltage in .csv file.
     */
    public static final int DEFAULT_VOLTAGE_COLUMN = 1;
    public static final int SKIPPING_CONSTANT = 2;

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    public static Trace loadTrace(String filePath, boolean notSkipping) throws IOException {
        return DataLoader.importFromCsv(filePath, DEFAULT_TIME_COLUMN, DEFAULT_VOLTAGE_COLUMN, notSkipping);
    }
    /**
     * Returns true when to skip, and false otherwise
     * @param indexInOriginalArray
     * @return 
     */
    public static boolean skipFunction(int indexInOriginalArray) {
        return indexInOriginalArray % SKIPPING_CONSTANT != 0;
    }
    
    public static int modifiedToOriginalIndex(int indexInModifiedArray) {
        return indexInModifiedArray * SKIPPING_CONSTANT;
    }
    
    /**
     * Method used to save data to file specified in @param dataPath.
     * This method saves whole trace.
     * 
     * @param filePath
     * @param trace
     * @throws IOException 
     */
    public static void saveTrace(String filePath, Trace trace) throws IOException {
        DataSaver.exportToCsv(trace, filePath);
    }
    
    /**
     * Method used to save data to file specified in @param dataPath between two indices.
     * 
     * @param filePath
     * @param trace
     * @param firstIndex
     * @param lastIndex
     * @throws IOException 
     */
    public static void saveTrace(String filePath, Trace trace, int firstIndex, int lastIndex) throws IOException {
        DataSaver.exportToCsv(trace, filePath, firstIndex, lastIndex);
    }
}
