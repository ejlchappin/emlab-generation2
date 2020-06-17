/**
 *
 */
package emlab.gen.trend;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Reads time series from a CSV file. Formatting must correspond to a format
 * where each row contains one time series and the first column contains the
 * variable names.
 *
 * Alternatively, if no {@link variableName} is given, it reads a CSV file with
 * a single column, in which each row contains a single value.
 *
 * @author JCRichstein
 *
 */
public class TimeSeriesCSVReader extends TimeSeriesImpl {

    Logger logger = Logger.getGlobal();

    private String filename;

    private String delimiter;

    private String variableName;

    private void readSingleColumn() {

        logger.info("Trying to read single column CSV file: " + filename);

        String data = new String();

        // Save the data in a long String
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(filename));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            int lineCounter = 0;
            while ((line = bufferedReader.readLine()) != null) {
                data = data.concat(line + ",");
                lineCounter++;
            }
            bufferedReader.close();
            double[] timeSeries = new double[lineCounter];
            int i = 0;
            for (String s : data.split("[,]")) {
                timeSeries[i] = Double.parseDouble(s);
                i++;
            }
            setTimeSeries(timeSeries);

        } catch (Exception e) {
            logger.severe("Couldn't read CSV file: " + filename);
            e.printStackTrace();
        }

    }

    private void readVariableFromCSV() {
        logger.info("Trying to read variable " + variableName + " from CSV file: " + filename + " with delimiter "
                + delimiter);

        // Save the data in a long String
        try {

            InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(filename));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            String[] lineContentSplit = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(variableName)) {
                    lineContentSplit = line.split(delimiter);
                    break;
                }
            }
            bufferedReader.close();
            double[] timeSeries = new double[lineContentSplit.length - 1];
            int i = 0;
            for (String s : lineContentSplit) {
                if (i > 0) {
                    timeSeries[i - 1] = Double.parseDouble(s);
                }
                i++;
            }
            setTimeSeries(timeSeries);

        } catch (Exception e) {
            logger.severe("Couldn't read CSV file: " + filename);
            e.printStackTrace();
        }

    }

    @Override
    public double getValue(long time) {
        if (timeSeries == null) {
            if (variableName != null) {
                readVariableFromCSV();
            } else {
                readSingleColumn();
            }
        }
        return super.getValue(time);
    }

    @Override
    public double[] getTimeSeries() {
        // double check with getValue(0) whether the time series has been read.
        if(timeSeries == null){
            getValue(0);
        }
        return timeSeries;
    }

    public void readCSVFile() {
        if (variableName != null) {
            readVariableFromCSV();
        } else {
            readSingleColumn();
        }
    }
    
    public void readCSVVariable(String variableName){
        setVariableName(variableName);
        readCSVFile();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variable) {
        this.variableName = variable;
    }

}
