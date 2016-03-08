package geneos_notification.controllers;

import java.io.IOException;

/*
 * Create by:	cmorley 10/09/2015
 * Description:	This is the main controller for the server and defines the commands that can be issued by cURL and the values and 
 * 				request types they must include in order to be accepted. Some key values for the server are stored here for 
 * 				convenience. Below is a list of the available commands that can be issued to the server with a description of their
 * 				operation. This class also, currently, contains the point of creation for the monitoring threads which are then 
 * 				controller and executed in the AlertController class. This Class also contains the list of logged in users which
 * 				is referenced for multiple device login's and whenever a change is made in relation to an account in order to verify
 * 				the user is who they say and are authorised to make those changes. The Class also contains a list of the currently
 * 				active threads within an object map which stores the critical data for those threads in order to control them 
 * 				externally.
*/

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itrsgroup.openaccess.commands.CommandExecution;

import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.ThreadItem;
import geneos_notification.startup_and_system_operations.DataviewListGenerator;
import geneos_notification.thread_operations.RowGenerator;
import geneos_notification.thread_operations.OACommander;


import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/*
 Call List:
 	- login(String,String,String)		= Used to log in user via username, password combo and assign registry key to user. Username first check for status (logged in), if negative then login verified via SQL database
 	- viewsystemkeys() 					= Used to display the two user dependent values of the server, the OA and SQL addresses. 
 	- threadtestt()						= POC to prove sever can run multiple instances of monitoring severity, used as callable producing Future for individual termination
 	- setcustomdv(String, String, int)	= Add or update a custom data view for an entity relevant to a particular user
 */

@RestController
public class InterfaceController {

  //  private static final String template = "Hello, %s!";
   // private final AtomicLong counter = new AtomicLong();
    //private final AtomicLong keycounter = new AtomicLong();
    private static String sqlKey;
    private static String oaKey;
    public static int sampleRate;
    public static ArrayList<JSONObject> currentDataviewEntityList;
    static LtA logA = new LogObject();

    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    //method=RequestMethod.POST)	
    
    @RequestMapping(value="/login", method=RequestMethod.POST)		
    public static String login(@RequestParam(value="username", defaultValue="") String username, @RequestParam(value="android_id", defaultValue="") String android_id, @RequestParam(value="key", defaultValue="") String key) throws Exception 
    {
    	String result = UserController.login(username, android_id, key);
    	return result;
    }
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value="/getMyDv", method=RequestMethod.POST)		
	public static String getMyDv(@RequestParam(value="username", defaultValue="") String username) 
	{
		// String result = DatabaseController.getUserDataviewList(username).toString();
		String result = null;
		ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
		ArrayList<String> userPaths = DatabaseController.getUserDataviewList(username);
		for(String path : userPaths)
		{
			if(DataviewListGenerator.list.contains(path))
			{
				objects.add(currentDataviewEntityList.get(DataviewListGenerator.list.indexOf(path)));
			}
		}
		result = objects.toString();
		return result;
	}
     
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public static String logout(@RequestParam(value = "username", defaultValue = "") String username,
			@RequestParam(value = "android_id", defaultValue = "") String android_id) throws Exception {
		String result = UserController.logout(username, android_id);
		// TransmissionHandler.additionMessage(userName, xpath);
		return result;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@RequestMapping(value="/setcustomdv", method=RequestMethod.POST)
public static String setCustomDV(@RequestParam(required = true, value="xpath", defaultValue="") String xpath, @RequestParam(required = true, value="username", defaultValue="") String userName) throws IOException, JSONException
{
	String path = xpath.trim();
	String result = UserController.setCustomDV(path, userName);
	TransmissionHandler.sendUpdateNotificaiton(path, userName, "dvAdd");
	return result;
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////// 

@RequestMapping(value="/removedv", method=RequestMethod.POST)
public static void removeDataview(@RequestParam(value="xpath", defaultValue="") String xpath, @RequestParam(value="username", defaultValue="") String userName) throws IOException, JSONException
{
	String path = xpath.trim();
	System.out.println("Removal xpath is here :" + path);
	TransmissionHandler.sendUpdateNotificaiton(path, userName, "dvRem");
	ThreadController.removeUserFromNotifyList(userName, path);
	UserController.userObjects.get(userName).removeDV(path);
	// TransmissionHandler.removeMessage(userName, xpath);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*@RequestMapping(value="/editdv", method=RequestMethod.POST)
public void editDV(@RequestParam(value="aentity", defaultValue="") String aentity, @RequestParam(value="rxpath", defaultValue="") String rxpath, @RequestParam(value="axpath", defaultValue="") String axpath, @RequestParam(value="username", defaultValue="") String userName) throws IOException
{
	ThreadController.editDV(rxpath, axpath, userName);
}*/

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// , method=RequestMethod.GET)
    @RequestMapping(value="/viewsystemkeys", method=RequestMethod.GET)					
    public static String viewSystemKeys() 
    {
    	String together = sqlKey + "  " + oaKey;
        return together;
    }
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// , method=RequestMethod.GET)
    @RequestMapping(value="/verifydev", method=RequestMethod.GET)					
	public static String verifyDevice(@RequestParam(value="dev_id", defaultValue="") String android_id, @RequestParam(value="verification", defaultValue="") String verification) 
	{
		DatabaseController.verifyStoredDevice(android_id, verification);
		logA.doLog("Controller" , "Veritication email sent for devioce : " + android_id, "Info");
		return "<!DOCTYPE html><html><font face=\"interface,sans-serif\"><head><title>Geneos Notification App Device Registrations success</title></head><body><center><img src=\"https://www.itrsgroup.com/sites/all/themes/bootstrap_sub_theme/logo.png\" alt=\"logo.com\" width=\"100\" height=\"40.5\"><h1>Device subscribed successfully to Geneos Notification Server.</h1><p>Your device has been successfully registered to your account for the Geneos Notification App.</p><p>If you experience any problems connecting your device please contact your database administrator to verify the devices associated with your account. You will now be able to log into your Notification server with this device without any further authentication.</p><small><p>© ITRS 2015, ALL RIGHTS RESERVED - Created by Connor Morley & Daniel Ratnaras </font></center></body></html>";
	}
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// , method=RequestMethod.GET)***************** NEEDS TESTING ********************

    @RequestMapping(value="/getAllDataview", method=RequestMethod.GET)			// First attempt to thread all DV list requests, needs testing		
	public static String getAllDataview() throws JSONException, InterruptedException, ExecutionException 
	{
    	logA.doLog("Controller" , "Dataview list requested", "Info");
    	return currentDataviewEntityList.toString();
	}
    
    // TESTING ONLY!!!!
/*    @RequestMapping(value="/killdvm", method=RequestMethod.GET)			// First attempt to thread all DV list requests, needs testing		
	public static void killdvm() throws JSONException, InterruptedException, ExecutionException 
	{
    	ThreadController.DVM.cancel(true);
	}*/
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
    
    @RequestMapping(value="/updatedv", method=RequestMethod.GET)
    public static void updateDV() throws InterruptedException, ExecutionException
    {
    	logA.doLog("Controller" , "[DVLIST UPDATE]Gateway setup alteration detected (Hooks), Dataview List is updating", "Info");
    	ExecutorService exec = Executors.newSingleThreadExecutor();
    	Callable<ArrayList<JSONObject>> callable = new Callable<ArrayList<JSONObject>>() {
    		@Override
    		public ArrayList<JSONObject> call() throws JSONException, InterruptedException{									
    			return DataviewListGenerator.collectDataviews();
    		}
    	};
    	Future<ArrayList<JSONObject>> future = exec.submit(callable);
    	ArrayList<JSONObject> dv = future.get();
    	exec.shutdown();
    	currentDataviewEntityList = dv;
    	logA.doLog("Controller" , "[DVLIST UPDATE]Dataview list was successfully updated.", "Info");
    }
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @RequestMapping(value="/getrow", method=RequestMethod.POST)					
	public static String getRow(@RequestParam(value="xpath", defaultValue="") String xpath) throws InterruptedException, ExecutionException
	{
    	logA.doLog("Controller" , "Row request made for row at xpath : " + xpath, "Info");
    	String ret = null;
    	ExecutorService exec = Executors.newSingleThreadExecutor();
    	Callable<String> callable = new Callable<String>() {
    		@Override
    		public String call() throws JSONException{
    			return RowGenerator.getRow(xpath).toString();
    		}
    	};
    	Future<String> future = exec.submit(callable);
    	ret = future.get();
    	exec.shutdown();
    	return ret;
	}
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/sendcommand", method = RequestMethod.POST)
	public static String sendCommand(@RequestParam(value = "xpath", defaultValue = "") String xpath, @RequestParam(value = "command", defaultValue = "") String command, @RequestParam(value = "username", defaultValue = "") String username)
			throws InterruptedException, ExecutionException {
		logA.doLog("Controller", "Command " + command + " has been issued for xpath : " + xpath, "Info");
		String ret = null;
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Callable<String> callable = new Callable<String>() {
			@Override
			public String call() throws JSONException {
				return OACommander.issueCommand(xpath, command, username);
			}
		};
		Future<String> future = exec.submit(callable);
		ret = future.get();
		exec.shutdown();
		return ret;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static void setKeyData(String sql, String oa)
    {
    	sqlKey = sql;
    	oaKey = oa;
    	DatabaseController.setAddress(sqlKey);
    	//DataviewListGenerator.setOaValue(oa);
    }
    
    public static String getOAkey()
    {
    	return oaKey;
    }
    
    public static ThreadItem getNotificationList(String xpath)
    {
    	return ThreadController.monitoringThreadList.get(xpath);
    }
    
}