package geneos_notification.loggers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogHandler {

	 private static String LOG_FILE_NAME = "NotificationServer.log";
     static Hashtable<String, Logger> loggers = new Hashtable<String, Logger>();
     static FileHandler handler = null;
     public static int limit;
     private static Level lvl;
     
     public static Logger getLogger(String loggerName) throws IOException {
         if ( loggers.get(loggerName) != null )
             return loggers.get(loggerName);

         if ( handler == null ) {
             boolean append = true;
             handler = new FileHandler(LOG_FILE_NAME, limit, 3, append);
             handler.setFormatter(new Formatter() {
                 @Override
                 public String format(LogRecord record) {
                     SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                     Calendar cal = new GregorianCalendar();
                     cal.setTimeInMillis(record.getMillis());
                     return record.getLevel()
                             + " " + logTime.format(cal.getTime())
                             + " || "
                             + record.getMessage() + "\n";
                 }
             });
             //handler.setFormatter(new SimpleFormatter());
         }

         Logger logger = Logger.getLogger(loggerName);
         logger.setLevel(lvl);
         logger.addHandler(handler);
         loggers.put(loggerName, logger);
         return logger;
     }
     
     public static void setLevel(String level)
     {
    	 if(level.equals("crtitical"))
    		 lvl = Level.SEVERE;
    	 if(level.equals("warning"))
    		 lvl = Level.WARNING;
    	if(level.equals("all"))
    		lvl = Level.ALL;
     }
	
}
