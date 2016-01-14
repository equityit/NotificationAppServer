package geneos_notification.startup_and_system_operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import geneos_notification.controllers.DatabaseController;
import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.GreetingController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.UserController;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.ThreadItem;
import geneos_notification.objects.User;
import scala.reflect.internal.Trees.This;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import java.time.LocalDateTime;


@SpringBootApplication
public class Application {
	
	public static ArrayList<String> settings = new ArrayList<String>();
	private final static Logger logger = Logger.getLogger(Application.class.getName());
	private static FileHandler fh = null;
	static LtA logA = new LogObject();
	//private final static Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		
/*		 try {
			 fh=new FileHandler(".\\loggerExample.log", false);
			 } catch (SecurityException | IOException e) {
			 e.printStackTrace();
			 }
		 Logger l = Logger.getLogger("");
		 fh.setFormatter(new SimpleFormatter());
		 l.addHandler(fh);
		 l.setLevel(Level.CONFIG);
		 logger.log(Level.INFO, "message 1");*/
		 
		//System.out.println("This does start");
		//LOGGER.setLevel(Level.INFO);
		//ConsoleHandler con = null;
		//LOGGER.addHandler(con);
		System.out.print("\n8888888888888888888888888b.  .d8888b. \n" +
				"  888      888    888   Y88bd88P  Y88b\n" +
				"  888      888    888    888Y88b.     \n" +
				"  888      888    888   d88P \"Y888b.  \n" +
				"  888      888    8888888P\"     \"Y88b.\n" +
				"  888      888    888 T88b        \"888\n" +
				"  888      888    888  T88b Y88b  d88P\n" +
				"8888888    888    888   T88b \"Y8888P\"  © \n" +
				"\n" +
				"<< Version 1.0 >>      << Created by C.Morley & D.Ratnaras 2015 >>\n" +
				"\n");
		logA.doLog("Application" , "Server Boot Initiated", "Info");
		start();
	}
	
    public static void start() throws InterruptedException, ExecutionException {
    	File file = new File(".\\settings.txt");
    	Scanner scnr = null;
    	try {
			scnr = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//LOGGER.log(Level.INFO, e.toString());
			e.printStackTrace();
			System.out.println("System Settings file not found - Server Terminating");
			logA.doLog("Application" , "System Settings file not found - Server Terminating", "Critical");
			System.exit(0);
		}
    	while(scnr.hasNext())
    	{
    		String line = scnr.nextLine();
    		settings.add(line);
    	}
    	if(settings.size() != 8)
    	{
    		System.out.println("System Settings file is incorrect - Not enough details - Server Terminating");
    		logA.doLog("Application" , "System Settings file is incorrect - Not enough details - Server Terminating", "Critical");
    		System.exit(0);
    	}
    	configureSettings(settings);
    	//LOGGER.log(Level.INFO, "COnfiguration successful");
    	logA.doLog("Application" , "Server Boot variables passed verification", "Info");
        SpringApplication.run(Application.class);
    }
    
    
    public static void configureSettings(ArrayList<String> settings) throws InterruptedException, ExecutionException
    {
    	String sqlServer = settings.get(0);
    	String smtpH = settings.get(1);
    	String smtpU = settings.get(2);
    	String smtpP = settings.get(3);
    	String OA = "geneos.cluster://" + settings.get(4) + ":" + settings.get(5) + "?username=" + settings.get(6) + "&password=" + settings.get(6);
    	GreetingController.setKeyData(sqlServer, OA);
    	EmailController.setDetails(smtpH, smtpU, smtpP);
    	checkValidity(OA, sqlServer);
    	GreetingController.updateDV();
    	perpetualReload();
    }
    
    public static void perpetualReload()
    {
    	Map<String,HashMap<String, String>> perpetualList = DatabaseController.getLiveDevices();
    	String xpathQuery = null;
		for (Map.Entry<String, HashMap<String, String>> entry : perpetualList.entrySet()) 
		{
			Map<String, String> devices = entry.getValue();
			for(Map.Entry<String, String> devEntry : devices.entrySet())
			{
				if(!UserController.userObjects.containsKey(entry.getKey()))
					UserController.userObjects.put(entry.getKey(), new User(entry.getKey(), devEntry.getKey(), devEntry.getValue()));
				else
					UserController.userObjects.get(entry.getKey()).addDevice(devEntry.getKey(), devEntry.getValue());			
			}
			if(xpathQuery == null)
				xpathQuery = "'" + entry.getKey() + "'";
			else
			{
				String append = " || '" + entry.getKey()+ "'";
				xpathQuery += append;
			}
		}
		Map<String,ArrayList<String>> livePaths = DatabaseController.getLivePaths(xpathQuery);
		for (Map.Entry<String, ArrayList<String>> entry : livePaths.entrySet())
		{
				ThreadController.monitoringThreadList.put(entry.getKey(), new ThreadItem(entry.getKey(), entry.getValue().get(0)));
				if(entry.getValue().size() != 1)
				{
					for(int i = 1; !entry.getValue().get(i).equals(null); i++)
						{
							ThreadController.monitoringThreadList.get(entry.getKey()).addUserID(entry.getValue().get(i));
						}
				}
				ThreadController.startPerpetualThread(entry.getKey());
		}
		
		
    }
    
    public static void checkValidity(String OA, String sqlServer)
    {
    	try{
    		DatabaseController.SQLConnect();
    		DatabaseController.close();
    	}
    	catch(Exception e)
    	{
    		//System.out.println("There was an error with the SQL configuration or address, please confirm details. System shutting down.");
    		logA.doLog("Application" , "There was an error with the SQL configuration or address, please confirm details. System shutting down.", "Critical");
    		System.exit(0);
    	}
    }
}
