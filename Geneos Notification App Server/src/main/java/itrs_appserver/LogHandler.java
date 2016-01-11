package itrs_appserver;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogHandler {

	 private static String LOG_FILE_NAME = "loggingTEST.log";
     static Hashtable<String, Logger> loggers = new Hashtable<String, Logger>();
     static FileHandler handler = null;
     
     public static Logger getLogger(String loggerName) throws IOException {
         if ( loggers.get(loggerName) != null )
             return loggers.get(loggerName);

         if ( handler == null ) {
             boolean append = true;
             handler = new FileHandler(LOG_FILE_NAME, append);
             handler.setFormatter(new SimpleFormatter());
         }

         Logger logger = Logger.getLogger(loggerName);
         logger.setLevel(Level.ALL);
         logger.addHandler(handler);
         loggers.put(loggerName, logger);
         return logger;
     }
	
}
