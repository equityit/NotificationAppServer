package itrs_appserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class Application {
	
	public static ArrayList<String> settings = new ArrayList<String>();
	//private final static Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	public static void main(String[] args)
	{
		//System.out.println("This does start");
		//LOGGER.setLevel(Level.INFO);
		//ConsoleHandler con = null;
		//LOGGER.addHandler(con);
		start();
	}
	
    public static void start() {
    	File file = new File("C:\\Users\\cmorley\\Documents\\settings.txt");
    	Scanner scnr = null;
    	try {
			scnr = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//LOGGER.log(Level.INFO, e.toString());
			e.printStackTrace();
			System.out.println("System Settings file not found - Server Terminating");
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
    		System.exit(0);
    	}
    	configureSettings(settings);
    	//LOGGER.log(Level.INFO, "COnfiguration successful");
        SpringApplication.run(Application.class);
    }
    
    
    public static void configureSettings(ArrayList<String> settings)
    {
    	String sqlServer = settings.get(0);
    	String smtpH = settings.get(1);
    	String smtpU = settings.get(2);
    	String smtpP = settings.get(3);
    	String OA = "geneos.cluster://" + settings.get(4) + ":" + settings.get(5) + "?username=" + settings.get(6) + "&password=" + settings.get(6);
    	GreetingController.setKeyData(sqlServer, OA);
    	MailRoom.setDetails(smtpH, smtpU, smtpP);
    }
}
