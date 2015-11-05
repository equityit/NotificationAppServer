package itrs_appserver;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.json.simple.JSONObject;
import org.jboss.netty.channel.socket.Worker;
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
			if ((entry.getValue()).getUsername().equals(username) && !entry.getValue().getDeviceKey(android_id).equals(null)) {
				return 1;
			}
		}
		return ret;
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

	public void addToNotifyList(String xpath, String userName) {
		if(monitoringThreadList.containsKey(xpath))
		{
			NotificationList current = monitoringThreadList.get(xpath);
			current.addUserID(userName);	// If an instance of monitoring for this xpath already exists add the user to the alerting list
			ArrayList<String> registrations = getRegistrationList(current);
			current.getFuture().cancel(true);
			Callable<Long> worker = new MyAnalysis(xpath, registrations);
			Future<Long> thread = executor.submit(worker);
			current.setFuture(thread);
			
		}
		
		else
		{
			System.out.println("This is the thread xpath : "+ xpath );
			ArrayList<String> registrations = getNewRegistrationList(userName);
			Callable<Long> worker = new MyAnalysis(xpath, registrations);
			Future<Long> thread = executor.submit(worker);
			monitoringThreadList.put(xpath, new NotificationList(xpath, thread, userName));	// If an instance of monitoring for this state does not already exist, create one and add user to the alerting list
			
		}
	}

	private void removeFromNotifyList(String xpath, String userName, String ret) {
		int status = monitoringThreadList.get(ret).removeUserID(userName);		// the user is already subscribed to that entity, remove subscription to the previous xpath of that entity
		if(status == 1)		// status value will be 1 if the monitoring is no longer subscribed to any users, as such it is stopped and the object deleted
		{
			monitoringThreadList.get(ret).closeMonitorthread(xpath);	// Terminates the monitoring thread, confirmed by submitted xpath match
			NotificationList destroy = monitoringThreadList.get(ret);	
			destroy = null;												// By setting the object to null it is marked as consumable and collected during garbage collection
		}
	}
	
	public ArrayList<String> getNewRegistrationList(String username)
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
	}
    
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
		return "EVerification Processed";
	}

    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// , method=RequestMethod.GET)

    @RequestMapping(value="/getAllDataview", method=RequestMethod.GET)					
	public String getAllDataview() throws JSONException 
	{
	return DataviewListGenerator.collectDataviews().toString();
	}
    
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
    	private ArrayList<String> registrationList = new ArrayList<String>();
    	
    	public MyAnalysis(String path, ArrayList<String> registration)
    	{
    		this.xpath = path;
    		this.registrationList = registration; 
    	}
    	
        @Override
        public Long call()
        {
        	System.out.println("THIS IS WITHIN THE THREAD PATH: " + xpath);
        	Long x = (long) 1;
            AlertController.startSample(xpath, registrationList);
			return x;
        }

    }
    
    public static void setKeyData(String sql, String oa)
    {
    	sqlKey = sql;
    	oaKey = oa;
    	SQLControl.setAddress(sqlKey);
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






/*@RequestMapping(value="/keytest", method=RequestMethod.POST)						
public Key keysend(@RequestParam(value="key", defaultValue="") String name)
{
	System.out.println(name);
	System.out.println("executed");
	return new Key(String.format(name), keycounter.incrementAndGet());
}*/

/*@RequestMapping(value="/greeting", method=RequestMethod.GET)
public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) 
{
    return new Greeting(counter.incrementAndGet(),
                        String.format(template, name));
}*/



/*    @RequestMapping(value="/sampletest", method=RequestMethod.POST)						
public void samplertest()
{
		  Runnable worker = new MyAnalysis("test"); //Define a runnable object and link it to a class that has can be implemented as Runnable with appropriate arguments.
		  executor.execute(worker);                           //Execute the defined runnable object/thread
}*/


//public static class MyAnalysis implements Runnable		// Thread, create the runnable object with the xpath then run the monitoring until cancled by future in "stopMonitoring" method
//{
//	
//	private String xpath;
//	
//	public MyAnalysis(String path)
//	{
//		this.xpath = path;
//	}
//	
//  @Override
//  public void run()
//  {
//      PushEpsilon.startSample(xpath);
//  }
//
//}






/*    @RequestMapping(value="/login", method=RequestMethod.POST)						
public String login(@RequestParam(value="username", defaultValue="") String username, @RequestParam(value="password", defaultValue="") String password, @RequestParam(value="key", defaultValue="") String key) throws NoSuchAlgorithmException 
{
	int userid = SQLControl.checkUser(username, password); // Grab userid from sql if the username and password are correct, otherwise return 0
	
	if (userid != 0) // If the user exists and details correct the id will not be 0, minimum of 1, so can add a new user object and return the map id to the android app for future server communication
	{
		int check = checkLoggedInStatus(userid); //Verify that user is not already logged in by comparing user id to user objects in user map
		if(check == 0)
		{
    	appUser holder = new appUser(username, password, key, userid);
    	userObjects.put(keycounter.incrementAndGet(),holder);
        return Objects.toString(keycounter.get());	// userid returned as string, must be formatted on the other end..... i think
		}
		else
		{
			return "User already logged in";
		}
	}
	
	else	// If username or password incorrect, or they don't exist, spit out generic error. 
	{
		return "Incorrect username of password";
	}
}*/

/*    public int checkLoggedInStatus(int id)
{
	int ret = 0;
	for(Map.Entry<Long, appUser> entry : userObjects.entrySet())
	{
		if((entry.getValue()).getID() == id)
		{
			return 1;
		}
	}
	return ret;
}*/


/* 
 * 
    @RequestMapping(value="/adddv", method=RequestMethod.GET)						
    public String adddv(@RequestParam(value="dvpath", defaultValue="") String name) 
    {
    
    	new Key(String.format(name), keycounter.incrementAndGet());
        return "Registration Key retrieved by App server Successfully";
    }
 
 
    @RequestMapping(value="/viewrequest", method=RequestMethod.GET)						
    public String viewrequest(@RequestParam(value="dvid", defaultValue="") String name) 
    {
    
    	new Key(String.format(name), keycounter.incrementAndGet());
        return "Registration Key retrieved by App server Successfully";
    }*/



/*    @RequestMapping(value="/JSONtest", method=RequestMethod.GET)
public String jsontest()
{
	String jtext = "";
	try{
	JSONObject test = new JSONObject();
	test.put("test", "x");
	test.put("example", "y");
	test.put("1", "z");
	test.put("2", "r");
	test.put("3", "t");
	StringWriter out = new StringWriter();
	test.writeJSONString(out);
	jtext = out.toString();
	
	}
	catch(IOException e)
	{
		System.out.println(e);
	}
	return jtext;
}*/



/*@RequestMapping(value="/setcustomdv", method=RequestMethod.POST)
public String setCustomDV(@RequestParam(value="entity", defaultValue="") String entity, @RequestParam(value="xpath", defaultValue="") String xpath, @RequestParam(value="userID", defaultValue="") String userID)
{
	String ret = "";
	appUser current = userObjects.get(Integer.parseInt(userID));
	
	ret = current.ammendCustomDV(entity, xpath);		// Returns either a result as seen in the comparison below, or xpath or previous custom DV in entity that user is subscribed too. Returned xpath used for updating.
	
	if(!ret.equals("Add Successful") && !ret.equals("Custom dataview already assigned")) // If something else is returned it is an xpath, used for updating the pre-existing entity dv for that user.
		removeFromNotifyList(xpath, userID, ret);
	if(!ret.equals("Custom dataview already assigned to this entity"))
		addToNotifyList(xpath, userID);	
	return ret;
}

private void addToNotifyList(String xpath, String userID) {
	if(monitoringThreadList.containsKey(xpath))
	{
		monitoringThreadList.get(xpath).addUserID(Integer.parseInt(userID));	// If an instance of monitoring for this xpath already exists add the user to the alerting list
	}
	
	else
	{
		Callable<Long> worker = new MyAnalysis("test");
		Future<Long> thread = executor.submit(worker);
		monitoringThreadList.put(xpath, new NotificationList(xpath, thread));	// If an instance of monitoring for this state does not already exist, create one and add user to the alerting list
		
	}
}

private void removeFromNotifyList(String xpath, String userID, String ret) {
	int status = monitoringThreadList.get(ret).removeUserID(Integer.parseInt(userID));		// the user is already subscribed to that entity, remove subscription to the previous xpath of that entity
	if(status == 1)		// status value will be 1 if the monitoring is no longer subscribed to any users, as such it is stopped and the object deleted
	{
		monitoringThreadList.get(ret).closeMonitorthread(xpath);	// Terminates the monitoring thread, confirmed by submitted xpath match
		NotificationList destroy = monitoringThreadList.get(ret);	
		destroy = null;												// By setting the object to null it is marked as consumable and collected during garbage collection
	}
}*/





/*
package itrs_appserver;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.json.simple.JSONObject;
import org.jboss.netty.channel.socket.Worker;
import org.json.JSONException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Future;


 Call List:
 	- login(String,String,String)		= Used to log in user via username, password combo and assign registry key to user. Username first check for status (logged in), if negative then login verified via SQL database
 	- viewsystemkeys() 					= Used to display the two user dependent values of the server, the OA and SQL addresses. 
 	- threadtestt()						= POC to prove sever can run multiple instances of monitoring severity, used as callable producing Future for individual termination
 	- setcustomdv(String, String, int)	= Add or update a custom data view for an entity relevant to a particular user
 

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
    
    @RequestMapping(value="/login", method=RequestMethod.POST)				
    public String login(@RequestParam(value="username", defaultValue="") String username, @RequestParam(value="android_id", defaultValue="") String android_id, @RequestParam(value="key", defaultValue="") String key) throws NoSuchAlgorithmException, SQLException 
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
	    			return "(0||3(+ )^+^"; // Collect data - tells device to scrape the user account on server for watch list	
	    		}
	    		else
	    		{
			    	appUser holder = new appUser(username, android_id, key, userid); // THIS NEEDS UPDATING TO NEW FORMAT!!!!!!!
			    	userObjects.put(username,holder);
			    	subscribeUserToStoredDataviews(username);
			        return "(0||3(+ )^+^";	// Collect data - tells device to scrape the user account on server for watch list	
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
			return "User already logged in, please ensure you are not logged in at another location";
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
			if ((entry.getValue()).getUsername().equals(username) && !entry.getValue().getDeviceKey(android_id).equals(null)) {
				return 1;
			}
		}
		return ret;
	}
    
    
    public void subscribeUserToStoredDataviews(String username)
    {
    	ArrayList<String> xpaths = new ArrayList<String>();
    	xpaths = SQLControl.getUserDataviewList(username);
    	for(String path : xpaths)
    	{
    		addToNotifyList(path, username);
    	}
    }
    
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
    	
    	if(!ret.equals("Add Successful") && !ret.equals("This dataview is already being monitored by this user")) // If something else is returned it is an xpath, used for updating the pre-existing entity dv for that user.
    		removeFromNotifyList(xpath, userName, ret);
    	if(ret.equals("Add Successful"))
    		addToNotifyList(xpath, userName);	
    	return ret;
    }

	public void addToNotifyList(String xpath, String userName) {
		if(monitoringThreadList.containsKey(xpath))
		{
			monitoringThreadList.get(xpath).addUserID(userName);	// If an instance of monitoring for this xpath already exists add the user to the alerting list
		}
		
		else
		{
			Callable<Long> worker = new MyAnalysis(xpath);
			Future<Long> thread = executor.submit(worker);
			monitoringThreadList.put(xpath, new NotificationList(xpath, thread, userName));	// If an instance of monitoring for this state does not already exist, create one and add user to the alerting list
			
		}
	}

	private void removeFromNotifyList(String xpath, String userName, String ret) {
		int status = monitoringThreadList.get(ret).removeUserID(userName);		// the user is already subscribed to that entity, remove subscription to the previous xpath of that entity
		if(status == 1)		// status value will be 1 if the monitoring is no longer subscribed to any users, as such it is stopped and the object deleted
		{
			monitoringThreadList.get(ret).closeMonitorthread(xpath);	// Terminates the monitoring thread, confirmed by submitted xpath match
			NotificationList destroy = monitoringThreadList.get(ret);	
			destroy = null;												// By setting the object to null it is marked as consumable and collected during garbage collection
		}
	}
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @RequestMapping(value="/viewsystemkeys", method=RequestMethod.GET)						
    public String viewSystemKeys() 
    {
    	String together = sqlKey + "  " + oaKey;
        return together;
    }
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping(value="/threadtest", method=RequestMethod.POST)						
    public void threadtest()
    {
    		  Callable<Long> worker = new MyAnalysis("test"); //Define a runnable object and link it to a class that has can be implemented as Runnable with appropriate arguments.
    		  Future<Long> example = executor.submit(worker);                          //Execute the defined runnable object/thread
    		  example.cancel(true);
    }
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// NON CALL METHODS AND THREADS //////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static class MyAnalysis implements Callable<Long>		// Thread, create the runnable object with the xpath then run the monitoring until cancled by future in "stopMonitoring" method
    {
    	
    	private String xpath;
    	
    	public MyAnalysis(String path)
    	{
    		this.xpath = path;
    	}
    	
        @Override
        public Long call()
        {
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






@RequestMapping(value="/keytest", method=RequestMethod.POST)						
public Key keysend(@RequestParam(value="key", defaultValue="") String name)
{
	System.out.println(name);
	System.out.println("executed");
	return new Key(String.format(name), keycounter.incrementAndGet());
}

@RequestMapping(value="/greeting", method=RequestMethod.GET)
public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) 
{
    return new Greeting(counter.incrementAndGet(),
                        String.format(template, name));
}



    @RequestMapping(value="/sampletest", method=RequestMethod.POST)						
public void samplertest()
{
		  Runnable worker = new MyAnalysis("test"); //Define a runnable object and link it to a class that has can be implemented as Runnable with appropriate arguments.
		  executor.execute(worker);                           //Execute the defined runnable object/thread
}


//public static class MyAnalysis implements Runnable		// Thread, create the runnable object with the xpath then run the monitoring until cancled by future in "stopMonitoring" method
//{
//	
//	private String xpath;
//	
//	public MyAnalysis(String path)
//	{
//		this.xpath = path;
//	}
//	
//  @Override
//  public void run()
//  {
//      PushEpsilon.startSample(xpath);
//  }
//
//}






    @RequestMapping(value="/login", method=RequestMethod.POST)						
public String login(@RequestParam(value="username", defaultValue="") String username, @RequestParam(value="password", defaultValue="") String password, @RequestParam(value="key", defaultValue="") String key) throws NoSuchAlgorithmException 
{
	int userid = SQLControl.checkUser(username, password); // Grab userid from sql if the username and password are correct, otherwise return 0
	
	if (userid != 0) // If the user exists and details correct the id will not be 0, minimum of 1, so can add a new user object and return the map id to the android app for future server communication
	{
		int check = checkLoggedInStatus(userid); //Verify that user is not already logged in by comparing user id to user objects in user map
		if(check == 0)
		{
    	appUser holder = new appUser(username, password, key, userid);
    	userObjects.put(keycounter.incrementAndGet(),holder);
        return Objects.toString(keycounter.get());	// userid returned as string, must be formatted on the other end..... i think
		}
		else
		{
			return "User already logged in";
		}
	}
	
	else	// If username or password incorrect, or they don't exist, spit out generic error. 
	{
		return "Incorrect username of password";
	}
}

    public int checkLoggedInStatus(int id)
{
	int ret = 0;
	for(Map.Entry<Long, appUser> entry : userObjects.entrySet())
	{
		if((entry.getValue()).getID() == id)
		{
			return 1;
		}
	}
	return ret;
}


 
 * 
    @RequestMapping(value="/adddv", method=RequestMethod.GET)						
    public String adddv(@RequestParam(value="dvpath", defaultValue="") String name) 
    {
    
    	new Key(String.format(name), keycounter.incrementAndGet());
        return "Registration Key retrieved by App server Successfully";
    }
 
 
    @RequestMapping(value="/viewrequest", method=RequestMethod.GET)						
    public String viewrequest(@RequestParam(value="dvid", defaultValue="") String name) 
    {
    
    	new Key(String.format(name), keycounter.incrementAndGet());
        return "Registration Key retrieved by App server Successfully";
    }



    @RequestMapping(value="/JSONtest", method=RequestMethod.GET)
public String jsontest()
{
	String jtext = "";
	try{
	JSONObject test = new JSONObject();
	test.put("test", "x");
	test.put("example", "y");
	test.put("1", "z");
	test.put("2", "r");
	test.put("3", "t");
	StringWriter out = new StringWriter();
	test.writeJSONString(out);
	jtext = out.toString();
	
	}
	catch(IOException e)
	{
		System.out.println(e);
	}
	return jtext;
}



@RequestMapping(value="/setcustomdv", method=RequestMethod.POST)
public String setCustomDV(@RequestParam(value="entity", defaultValue="") String entity, @RequestParam(value="xpath", defaultValue="") String xpath, @RequestParam(value="userID", defaultValue="") String userID)
{
	String ret = "";
	appUser current = userObjects.get(Integer.parseInt(userID));
	
	ret = current.ammendCustomDV(entity, xpath);		// Returns either a result as seen in the comparison below, or xpath or previous custom DV in entity that user is subscribed too. Returned xpath used for updating.
	
	if(!ret.equals("Add Successful") && !ret.equals("Custom dataview already assigned")) // If something else is returned it is an xpath, used for updating the pre-existing entity dv for that user.
		removeFromNotifyList(xpath, userID, ret);
	if(!ret.equals("Custom dataview already assigned to this entity"))
		addToNotifyList(xpath, userID);	
	return ret;
}

private void addToNotifyList(String xpath, String userID) {
	if(monitoringThreadList.containsKey(xpath))
	{
		monitoringThreadList.get(xpath).addUserID(Integer.parseInt(userID));	// If an instance of monitoring for this xpath already exists add the user to the alerting list
	}
	
	else
	{
		Callable<Long> worker = new MyAnalysis("test");
		Future<Long> thread = executor.submit(worker);
		monitoringThreadList.put(xpath, new NotificationList(xpath, thread));	// If an instance of monitoring for this state does not already exist, create one and add user to the alerting list
		
	}
}

private void removeFromNotifyList(String xpath, String userID, String ret) {
	int status = monitoringThreadList.get(ret).removeUserID(Integer.parseInt(userID));		// the user is already subscribed to that entity, remove subscription to the previous xpath of that entity
	if(status == 1)		// status value will be 1 if the monitoring is no longer subscribed to any users, as such it is stopped and the object deleted
	{
		monitoringThreadList.get(ret).closeMonitorthread(xpath);	// Terminates the monitoring thread, confirmed by submitted xpath match
		NotificationList destroy = monitoringThreadList.get(ret);	
		destroy = null;												// By setting the object to null it is marked as consumable and collected during garbage collection
	}
}*/