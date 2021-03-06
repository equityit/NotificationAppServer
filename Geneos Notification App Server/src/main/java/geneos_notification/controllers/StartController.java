package geneos_notification.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.catalina.connector.Connector;
import org.json.JSONException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.SocketUtils;

import geneos_notification.loggers.LogHandler;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.CustomDataView;
import geneos_notification.objects.ThreadItem;
import geneos_notification.objects.User;
import geneos_notification.startup_and_system_operations.DataviewListGenerator;
import geneos_notification.thread_operations.DataViewMonitor;
import scala.reflect.internal.Trees.This;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;


@SpringBootApplication
public class StartController {
	
/*	@Bean
	public Integer port() {
		return SocketUtils.findAvailableTcpPort();
	}
	
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());
		return tomcat;
	}
	
	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(8080);
		connector.setScheme("http");
		return connector;
	}*/
	
	//public static ArrayList<String> settings = new ArrayList<String>();
	public static Map<String, String> setting = new HashMap<String,String>();
	private final static Logger logger = Logger.getLogger(StartController.class.getName());
	private static FileHandler fh = null;
	static LtA logA = new LogObject();
	//private final static Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		System.out.print("\n8888888888888888888888888b.  .d8888b. \n" +
				"  888      888    888   Y88bd88P  Y88b\n" +
				"  888      888    888    888Y88b.     \n" +
				"  888      888    888   d88P \"Y888b.  \n" +
				"  888      888    8888888P\"     \"Y88b.\n" +
				"  888      888    888 T88b        \"888\n" +
				"  888      888    888  T88b Y88b  d88P\n" +
				"8888888    888    888   T88b \"Y8888P\"  © \n" +
				"\n" +
				"<< Version 1.3.1 >>      << Created by C.Morley & D.Ratnaras 2015/2016 >>\n" +
				"\n");
		start();
	}
    
    public static void start() throws InterruptedException, ExecutionException {
    	readSettingsFile();
    	configureSetting(setting);
    	InterfaceController.updateDV(); // Redundant?
    	perpetualReload();
    	ThreadController.startDVMonitor();
    	logA.doLog("Start" , "[Start]Server Boot variables passed verification", "Info");
        SpringApplication.run(StartController.class);
    }

public static void readSettingsFile(){
	//System.out.println(System.getProperty("os.name"));
	//System.getProperties().list(System.out);
	File file = null;
	if(System.getProperty("os.name").contains("Windows"))
	file = new File(".\\settings.properties");
	if(System.getProperty("os.name").contains("Linux"))
	file = new File("./settings.properties");
	
	
	try{
	InputStream input = new FileInputStream(file);
	Properties prop = new Properties();
	prop.load(input);
	
		setting.put("mySQLHost", prop.getProperty("mySQL_Host"));
		setting.put("mySQLDB", prop.getProperty("mySQL_Database_Name"));
		setting.put("mySQLUser", prop.getProperty("mySQL_Username"));
		setting.put("mySQLPass", prop.getProperty("mySQL_Password"));
		setting.put("SMTPHost", prop.getProperty("SMTP_Host"));
		setting.put("SMTPUser", prop.getProperty("SMTP_Username"));
		setting.put("SMTPPass", prop.getProperty("SMTP_Password"));
		setting.put("OAHost", prop.getProperty("OpenAccess_Host"));
		setting.put("OAPort", prop.getProperty("OpenAccess_Port"));
		setting.put("OAUser", prop.getProperty("OpenAccess_Username"));
		setting.put("OAPass", prop.getProperty("OpenAccess_Password"));
		setting.put("SampleRate", prop.getProperty("Sampling_Rate_(ms)"));
		setting.put("MaxLog", prop.getProperty("Maximum_Log_Size"));
		setting.put("LogLevel", prop.getProperty("Log_Level"));
	} catch(FileNotFoundException fe){
		fe.printStackTrace();
		logA.doLog("Start" , "[Start]Running Settings file not found - Server Terminating", "Critical");
		System.exit(0);
	} catch(IOException ie){
		ie.printStackTrace();
		logA.doLog("Start" , "[Start]Running Settings file could not be accessed correctly - Server Terminating", "Critical");
		System.exit(0);
	}
	
	
	
	
	File fileA = null;
	if(System.getProperty("os.name").contains("Windows"))
	fileA = new File(".\\application.properties");
	if(System.getProperty("os.name").contains("Linux"))
	fileA = new File("./application.properties");
	
	try{
	InputStream input = new FileInputStream(fileA);
	Properties prop = new Properties();
	prop.load(input);
	
		setting.put("ip", prop.getProperty("server.address"));
		setting.put("port", prop.getProperty("server.port"));
	if(prop.getProperty("server.ssl.key-store") != null)
		setting.put("secured", "true");
	else
		setting.put("secured", "false");
	} catch(FileNotFoundException fe){
		fe.printStackTrace();
		logA.doLog("Start" , "[Start]System Settings file not found - Server Terminating", "Critical");
		System.exit(0);
	} catch(IOException ie){
		ie.printStackTrace();
		logA.doLog("Start" , "[Start]System Settings file could not be accessed correctly - Server Terminating", "Critical");
		System.exit(0);
	}
	
	
/*	Scanner scnrA = null;
	try {
		scnrA = new Scanner(fileA);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		logA.doLog("Start" , "[Start]System Settings file not found - Server Terminating", "Critical");
		System.exit(0);
	}
	while(scnrA.hasNextLine())
	{
		String lineA = scnrA.nextLine();
		if (lineA.contains("server.address")) {
			setting.put("ip", lineA.substring(lineA.indexOf("=") + 1));
		} else if (lineA.contains("server.port")) {
			setting.put("port", lineA.substring(lineA.indexOf("=") + 1));
		} 
	}*/
	if(setting.size() != 17)
	{
		logA.doLog("Start" , "[Start]System Settings file is incorrect - Not enough details - Server Terminating", "Critical");
		System.exit(0);
	}
}
    
    public static void configureSetting(Map<String, String> setting) throws InterruptedException, ExecutionException
    {
    	String sqlServer = "jdbc:mysql://"+setting.get("mySQLHost")+"/"+setting.get("mySQLDB")+"?user="+setting.get("mySQLUser")+"&password="+setting.get("mySQLPass");
    	String smtpH = setting.get("SMTPHost");
    	String smtpU = setting.get("SMTPUser");
    	String smtpP = setting.get("SMTPPass");
    	String ip = setting.get("ip");
    	String port = setting.get("port");
    	String OA = "geneos.cluster://" + setting.get("OAHost") + ":" + setting.get("OAPort") + "?username=" + setting.get("OAUser") + "&password=" + setting.get("OAPass");
    	Boolean secured = Boolean.parseBoolean(setting.get("secured"));
    	InterfaceController.setKeyData(sqlServer, OA);
    	InterfaceController.sampleRate = Integer.parseInt(setting.get("SampleRate"));
    	EmailController.setDetails(smtpH, smtpU, smtpP, ip, port, secured);
    	LogHandler.limit = Integer.parseInt(setting.get("MaxLog"));
    	LogHandler.setLevel(setting.get("LogLevel"));
    	try{
    	checkValidity();
    	}
    	catch(Exception e)
    	{
    		logA.doLog("Start" , "[Start]Boot configuration error was encountered, please confirm settings and re-try boot. System shutting down.", "Critical");
    		System.exit(0);
    	}
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
				UserController.userObjects.get(entry.getValue().get(0)).pathList.add(new CustomDataView(entry.getKey()));
				if(entry.getValue().size() != 1)
				{
				/*	for(int i = 1; !entry.getValue().get(i).equals(null); i++)
						{
							ThreadController.monitoringThreadList.get(entry.getKey()).addUserID(entry.getValue().get(i));
						}*/
						for(int i = 1; i < entry.getValue().size(); i++)
					{
						ThreadController.monitoringThreadList.get(entry.getKey()).addUserID(entry.getValue().get(i));
						UserController.userObjects.get(entry.getValue().get(i)).pathList.add(new CustomDataView(entry.getKey()));
					}
				}
				ThreadController.startPerpetualThread(entry.getKey());
		}
		
		
    }
    
    public static void checkValidity()
    {
    	try{
    		DatabaseController.SQLConnect();
    		DatabaseController.close();
    	}
    	catch(Exception e)
    	{
    		//System.out.println("There was an error with the SQL configuration or address, please confirm details. System shutting down.");
    		logA.doLog("Start" , "[Start]There was an error with the SQL configuration or address, please confirm details.", "Critical");
    		throw new RuntimeException(e);
    	}
    }
}
