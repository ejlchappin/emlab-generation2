package emlab.gen.engine;

import java.util.logging.*;

class CSVLogFormatter extends Formatter {
    // Create a DateFormat to format the logger timestamp.

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        
        builder.append(record.getMillis()).append(";");
        builder.append(record.getSourceClassName()).append(";");
        builder.append(record.getSourceMethodName()).append(";");
        builder.append(record.getLevel()).append(";\"");
        builder.append(formatMessage(record)).append("\"");
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
    	
        StringBuilder builder = new StringBuilder(1000);
        builder.append("time").append(";");
        builder.append("class").append(";");
        builder.append("method").append(";");
        builder.append("level").append(";");
        builder.append("message");
        builder.append("\n");
        return builder.toString();
        
        //return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}