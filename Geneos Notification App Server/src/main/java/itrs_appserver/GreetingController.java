package itrs_appserver;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.Future;


/*
 Call List:
 	- login(String,String,String)		= Used to log in user via username, password combo and assign registry key to user. Username first check for status (logged in), if negative then login verified via SQL database
 	- viewsystemkeys() 					= Used to display the two user dependent values of the server, the OA and SQL addresses. 
 	- threadtestt()						= POC to prove sever can run multiple instances of monitoring severity, used as callable producing Future for individual termination
 	- setcustomdv(String, String, int)	= Add or update a custom data view for an entity relevant to a particular user
 */

@RestController
public class GreetingController {

  //  private static final String template = "Hello, %s!";
   // private final AtomicLong counter = new AtomicLong();
    //private final AtomicLong keycounter = new AtomicLong();
    private static String sqlKey;
    private static String oaKey;
    public static Map<String, appUser> userObjects = new HashMap<String, appUser>();
    public static Map<String,NotificationList> monitoringThreadList = new HashMap<String, NotificationList>();
    private ExecutorService executor = Executors.newFixedThreadPool(200);
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    //method=RequestMethod.POST)	
    
    @RequestMapping(value="/login", method=RequestMethod.POST)		
    public String login(@RequestParam(value="username", defaultValue="") String username, @RequestParam(value="android_id", defaultValue="") String android_id, @RequestParam(value="key", defaultValue="") String key) throws Exception 
    {
    	try{
		int check = checkLoggedInStatus(username, android_id); //Change this to search for android_id so the same machine cant have two instances???
		if(check == 0)
		{
	    	int userid = SQLControl.checkUser(username, android_id); // Grab userid from sql if the username and password are correct, otherwise return 0
	    	System.out.println("this executed as login");
	    	
	    	if (userid == 2) // user exists and the android_id is to a validated device - Allow access and subscribe device to user monitored data views
	    	{
	    		if(userObjects.containsKey(username))
	    		{
	    			appUser holder = userObjects.get(username);
	    			holder.addDevice(android_id, key);
	    			refreshDeviceToStoredDataviews(username);
	    			return "successfully logged in"; // Collect data - tells device to scrape the user account on server for watch list	
	    		}
	    		else
	    		{
			    	appUser holder = new appUser(username, android_id, key, userid); // THIS NEEDS UPDATING TO NEW FORMAT!!!!!!!
			    	userObjects.put(username,holder);
			    	subscribeUserToStoredDataviews(username);
			        return "successfully logged in";	// Collect data - tells device to scrape the user account on server for watch list	
		    	}
	    	}
	    	
	    	if (userid == 1) // User exists but the android_id is not to a valid device
	    	{		// YOU NEED TO ADD A LIMITER TO CATCH DUPLICATE ATTEMPTS OF THE SAME DEVICE WITHOUT IT BEING AUTHORISED
	    		
	    		int deviceCount = SQLControl.checkDeviceExistence(android_id);
	    		if(deviceCount!=0)
	    			return "This device has already been registered, please check your email for a verification code";
	    		SQLControl.createInValidDevice(username, android_id, key);
	    		return "We have sent you a device authorisation email, please enter the code provided to verify this device";
	    	}
	    	
	    	else 	// If user name is not present in the database		
	    	{	
				int x = SQLControl.checkValidDomain(username);
				
				if(x != 0) // user name/email has a whitelist domain so they are allowed to be a user
				{
					SQLControl.createUser(username, android_id, key);
					return "Since you are a new user you will need to verify your device. We have sent you an email, please enter the code provided to activate your device";
				}
				else
					return "You are not authorised to use this app, please contact your local administrator";
	    	}
	    	
	    	
	    	
		} else {
			return "successfully logged in";
		}
    	}
    	catch(RuntimeException e)
    	{
    		e.printStackTrace();
    		return "An error occured - Please contact your administrator";
    	}
		
    }
    
    
    public int checkLoggedInStatus(String username, String android_id)		// Checks whether a user is already logged in with that username - as such usernames must be UNIQUE (MYSQL adaptation required)
	{
		int ret = 0;
		for (Map.Entry<String, appUser> entry : userObjects.entrySet()) {
			if ((entry.getValue()).getUsername().equals(username) && entry.getValue().deviceList.containsKey(android_id)) {
				return 1;
			}
		}
		return ret;
	}
    
    public void refreshDeviceToStoredDataviews(String username)
    {
    	ArrayList<String> xpaths = new ArrayList<String>();
    	xpaths = SQLControl.getUserDataviewList(username);
    	for(String path : xpaths)
    	{
    		System.out.println(path);
    		restartNotifyList(path);
    		System.out.println("The path being restarted :" + path);
    	}
    	
    }
    
    public void subscribeUserToStoredDataviews(String username)
    {
    	ArrayList<String> xpaths = new ArrayList<String>();
    	appUser current = userObjects.get(username);
    	xpaths = SQLControl.getUserDataviewList(username);
    	for(String path : xpaths)
    	{
    		System.out.println(path);
    		current.ammendCustomDV("DUMMY", path);
    		addToNotifyList(path, username);
    	}
    	
    }
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public String logout(@RequestParam(value = "username", defaultValue = "") String username,
			@RequestParam(value = "android_id", defaultValue = "") String android_id) throws Exception {
		int deviceCheck = userObjects.get(username).removeDevice(android_id);
		System.out.println("The status of the device check is :" + deviceCheck);
		if (deviceCheck == 1) {
			System.out.println("User removed");
			removeUserFromDataviews(username);
			userObjects.remove(username);
		} else {
			System.out.println("Device removed");
			refreshDeviceToStoredDataviews(username);
		}

		return "This device has been successfully logged out";
	}

	public void removeUserFromDataviews(String username) {
		ArrayList<String> xpaths = new ArrayList<String>();
		xpaths = SQLControl.getUserDataviewList(username);
		for (String path : xpaths) {
			System.out.println(path);
			removeUserFromNotifyList(username, path);
			System.out.println("The path being restarted :" + path + "   For User :" + username);
		}

	}

	public void removeUserFromNotifyList(String username, String xpath) {
		NotificationList current = monitoringThreadList.get(xpath);
		current.getFuture().cancel(true);
		int number = current.removeUser(username);
		System.out.println("This is the returned number : " + number);
		if (number == 1) {
			current = null;
		} else if (number == 0) {
			System.out.println("It shouldn't get in here");
			Callable<Long> worker = new MyAnalysis(xpath);
			Future<Long> thread = executor.submit(worker);
			current.setFuture(thread);
		}
	}

/*	private void removeFromNotifyList(String xpath, String userName, String ret) {
int status = monitoringThreadList.get(ret).removeUserID(userName);		// the user is already subscribed to that entity, remove subscription to the previous xpath of that entity
if(status == 1)		// status value will be 1 if the monitoring is no longer subscribed to any users, as such it is stopped and the object deleted
{
monitoringThreadList.get(ret).closeMonitorthread(xpath);	// Terminates the monitoring thread, confirmed by submitted xpath match
NotificationList destroy = monitoringThreadList.get(ret);	
destroy = null;												// By setting the object to null it is marked as consumable and collected during garbage collection
}
}
*/

    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@RequestMapping(value="/setcustomdv", method=RequestMethod.POST)
public String setCustomDV(@RequestParam(value="entity", defaultValue="") String entity, @RequestParam(value="xpath", defaultValue="") String xpath, @RequestParam(value="username", defaultValue="") String userName)
{
String ret = "";

if(!userObjects.containsKey(userName)) // Capture for if the user exists in the system, can't add to a user that does not exist
return "You are not a valid user";

appUser current = userObjects.get(userName);

ret = current.ammendCustomDV(entity, xpath);		// Returns either a result as seen in the comparison below, or xpath or previous custom DV in entity that user is subscribed too. Returned xpath used for updating.

/*    	if(!ret.equals("Add Successful") && !ret.equals("This dataview is already being monitored by this user")) // If something else is returned it is an xpath, used for updating the pre-existing entity dv for that user.
removeFromNotifyList(xpath, userName, ret);*/
if(ret.equals("Add Successful"))
addToNotifyList(xpath, userName);	
return ret;
}


public void restartNotifyList(String xpath)
{
NotificationList current = monitoringThreadList.get(xpath);
current.getFuture().cancel(true);
current.resetDevices();
Callable<Long> worker = new MyAnalysis(xpath);
Future<Long> thread = executor.submit(worker);
current.setFuture(thread);
}

public void addToNotifyList(String xpath, String userName) {
if(monitoringThreadList.containsKey(xpath))
{
NotificationList current = monitoringThreadList.get(xpath);
current.addUserID(userName);	// If an instance of monitoring for this xpath already exists add the user to the alerting list
//ArrayList<String> registrations = getRegistrationList(current);
current.getFuture().cancel(true);
Callable<Long> worker = new MyAnalysis(xpath);
Future<Long> thread = executor.submit(worker);
current.setFuture(thread);

}

else
{
System.out.println("This is the thread xpath : "+ xpath );
//ArrayList<String> registrations = getNewRegistrationList(userName);
Callable<Long> worker = new MyAnalysis(xpath);
Future<Long> thread = executor.submit(worker);
monitoringThreadList.put(xpath, new NotificationList(xpath, thread, userName));	// If an instance of monitoring for this state does not already exist, create one and add user to the alerting list

}
}


/*	public ArrayList<String> getNewRegistrationList(String username)
{
ArrayList<String> ret = new ArrayList<String>();
ArrayList<String> regs = userObjects.get(username).getRegistrations();
for(String reg : regs)
{
ret.add(reg);
}
return ret;
}

public ArrayList<String> getRegistrationList(NotificationList now)
{
ArrayList<String> ret = new ArrayList<String>();
ArrayList<String> stock = now.getUsers();
for (String username : stock)
{
ArrayList<String> regs = userObjects.get(username).getRegistrations();
for(String reg : regs)
{
ret.add(reg);
}
}
return ret;
}*/
    
    

    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// , method=RequestMethod.GET)
    @RequestMapping(value="/viewsystemkeys", method=RequestMethod.GET)					
    public String viewSystemKeys() 
    {
    	String together = sqlKey + "  " + oaKey;
        return together;
    }
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// , method=RequestMethod.GET)
    @RequestMapping(value="/verifydev", method=RequestMethod.GET)					
	public String verifyDevice(@RequestParam(value="dev_id", defaultValue="") String android_id, @RequestParam(value="verification", defaultValue="") String verification) 
	{
		SQLControl.verifyStoredDevice(android_id, verification);
		return "<!DOCTYPE html><html><body><img src=\".\\logo.png\" alt=\"logo.com\" width=\"100\" height=\"140\"><h1>Verification Processed</h1></body></html>";
	}
    
    @RequestMapping(value="/test")		
	public String test() 
	{
    	return "<!DOCTYPE html><html><font face=\"interface,sans-serif\"><head><title>Geneos Notification App Device Registrations success</title></head><body><center><img src=\"https://www.itrsgroup.com/sites/all/themes/bootstrap_sub_theme/logo.png\" alt=\"logo.com\" width=\"100\" height=\"40.5\"><h1>Device subscribed successfully to Geneos Notification Server.</h1><p>Your device has been successfully registered to your account for the Geneos Notification App.</p><p>If you experience any problems connecting your device please contact your database administrator to verify the devices associated with your account. You will now be able to log into your Notification server with this device without any further authentication.</p><small><p>© ITRS 2015, ALL RIGHTS RESERVED - Created by Connor Morley & Daniel Ratnaras </font></center></body></html>";
	}

    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// , method=RequestMethod.GET)***************** NEEDS TESTING ********************

    @RequestMapping(value="/getAllDataview", method=RequestMethod.GET)			// First attempt to thread all DV list requests, needs testing		
	public String getAllDataview() throws JSONException, InterruptedException, ExecutionException 
	{
    	
    	ExecutorService exec = Executors.newSingleThreadExecutor();
    	Callable<String> callable = new Callable<String>() {
    		@Override
    		public String call() throws JSONException{
    			return DataviewListGenerator.collectDataviews().toString();
    		}
    	};
    	Future<String> future = exec.submit(callable);
    	String dv = future.get();
    	exec.shutdown();
    	return dv;
	}
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /*@RequestMapping(value="/refreshView", method=RequestMethod.GET)					
	public String getRefreshView(@RequestParam(value="expath", defaultValue="") String expath) throws JSONException 
	{
    	return DataviewListGenerator.collectDataviews().toString();
	}*/
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
   /* @RequestMapping(value="/threadtest", method=RequestMethod.POST)						
    public void threadtest()
    {
    		  Callable<Long> worker = new MyAnalysis("test"); //Define a runnable object and link it to a class that has can be implemented as Runnable with appropriate arguments.
    		  Future<Long> example = executor.submit(worker);                          //Execute the defined runnable object/thread
    		  example.cancel(true);
    }*/
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// NON CALL METHODS AND THREADS //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static class MyAnalysis implements Callable<Long>		// Thread, create the runnable object with the xpath then run the monitoring until cancled by future in "stopMonitoring" method
    {
    	
    	private String xpath;
    	//private ArrayList<String> registrationList = new ArrayList<String>();
    	
    	public MyAnalysis(String path)
    	{
    		this.xpath = path;
    		//this.registrationList = registration; 
    	}
    	
        @Override
        public Long call() throws InterruptedException
        {
        	System.out.println("THIS IS WITHIN THE THREAD PATH: " + xpath);
        	Long x = (long) 1;
            AlertController.startSample(xpath);
			return x;
        }

    }
    
    public static void setKeyData(String sql, String oa)
    {
    	sqlKey = sql;
    	oaKey = oa;
    	SQLControl.setAddress(sqlKey);
    	//DataviewListGenerator.setOaValue(oa);
    }
    
    public static String getOAkey()
    {
    	return oaKey;
    }
    
    public static NotificationList getNotificationList(String xpath)
    {
    	return monitoringThreadList.get(xpath);
    }
    
}