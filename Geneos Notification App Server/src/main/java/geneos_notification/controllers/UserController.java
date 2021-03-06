package geneos_notification.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;

import geneos_notification.controllers.ThreadController.MyAnalysis;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.User;

public class UserController {
	static LtA logA = new LogObject();
	public static Map<String, User> userObjects = new HashMap<String, User>();
	
	public static String login(String username, String android_id, String key) throws Exception
	{   	
	    	logA.doLog("UserController" , "[U-Controller]Login attempt with username : " + username , "Info");
	    	try{
			int check = checkLoggedInStatus(username, android_id); //Change this to search for android_id so the same machine cant have two instances???
			if(check == 0)
			{
		    	int userid = DatabaseController.checkUser(username, android_id); // Grab userid from sql if the username and password are correct, otherwise return 0
		    	if (userid == 2) // user exists and the android_id is to a validated device - Allow access and subscribe device to user monitored data views
		    	{
		    		if(UserController.userObjects.containsKey(username))
		    		{
		    			User holder = UserController.userObjects.get(username);
		    			holder.addDevice(android_id, key);
		    			DatabaseController.loginDevice(android_id);
		    			DatabaseController.updateDeviceKey(android_id, key);
		    			refreshDeviceToStoredDataviews(username);
		    			logA.doLog("UserController" , "[U-Controller]" + username + " logged in successfully on device " + android_id, "Info");
		    			return "successfully logged in"; // Collect data - tells device to scrape the user account on server for watch list	
		    		}
		    		else
		    		{
				    	User holder = new User(username, android_id, key); // THIS NEEDS UPDATING TO NEW FORMAT!!!!!!!
				    	UserController.userObjects.put(username,holder);
				    	DatabaseController.loginDevice(android_id);
				    	DatabaseController.updateDeviceKey(android_id, key);
				    	subscribeUserToStoredDataviews(username);
				    	logA.doLog("UserController" , "[U-Controller]Initial log on for " + username + " with device " + android_id, "Info");
				        return "successfully logged in";	// Collect data - tells device to scrape the user account on server for watch list	
			    	}
		    	}
		    	
		    	if (userid == 1) // User exists but the android_id is not to a valid device
		    	{		// YOU NEED TO ADD A LIMITER TO CATCH DUPLICATE ATTEMPTS OF THE SAME DEVICE WITHOUT IT BEING AUTHORISED
		    		
		    		int deviceCount = DatabaseController.checkDeviceExistence(android_id);
		    		if(deviceCount!=0){
		    			logA.doLog("UserController" , "[U-Controller]" + username + " has already been sent a verification email for " + android_id, "Info");
		    			return "This device has already been registered, please check your email for a verification URL";
		    		}
		    		DatabaseController.createInValidDevice(username, android_id, key);
		    		logA.doLog("UserController" , "[U-Controller]" + username + " has been sent a verification email for " + android_id, "Info");
		    		return "We have sent you a device authorisation email, please follow the URL provided to verify this device";
		    	}
		    	
		    	else 	// If user name is not present in the database		
		    	{	
					int x = DatabaseController.checkValidDomain(username);
					
					if(x != 0) // user name/email has a whitelist domain so they are allowed to be a user
					{
						DatabaseController.createUser(username, android_id, key);
						logA.doLog("UserController" , "[U-Controller]New user " + username + " has attempted to login, verification email sent and account is in probation.", "Info");
						return "Since you are a new user you will need to verify your device. We have sent you an email, please follow the contained URL to verify your device";
					}
					else
					{
						logA.doLog("UserController" , "[U-Controller]Attempted login made by unauthorised user with username : " + username, "Warning");
						return "You are not authorised to use this app, please contact your local administrator";
					}
		    	}
		    	
		    	
		    	
			} else {
				if(!userObjects.get(username).getDeviceKey(android_id).equals(key))
				{
					DatabaseController.updateDeviceKey(android_id, key);
					userObjects.get(username).removeDevice(android_id);
					userObjects.get(username).addDevice(android_id, key);
					refreshDeviceToStoredDataviews(username);
				}
				return "successfully logged in";
			}
	    	}
	    	catch(RuntimeException e)
	    	{
	    		e.printStackTrace();
	    		logA.doLog("UserController" , "[U-Controller]Login error has occured, please see associated stack trace for more information. Username : " + username + " android_id : " + android_id, "Critical");
	    		logA.doLog("UserController" , e.toString(), "Info");
	    		return "An error occured - Please contact your administrator";
	    	}
			
	    }
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static int checkLoggedInStatus(String username, String android_id)		// Checks whether a user is already logged in with that username - as such usernames must be UNIQUE (MYSQL adaptation required)
	{
		int ret = 0;
		for (Map.Entry<String, User> entry : UserController.userObjects.entrySet()) {
			if ((entry.getValue()).getUsername().equals(username) && entry.getValue().deviceList.containsKey(android_id)) {
				return 1;
			}
		}
		return ret;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static int refreshDeviceToStoredDataviews(String username)
    {
		int res = 0;
    	ArrayList<String> xpaths = new ArrayList<String>();
    	xpaths = DatabaseController.getUserDataviewList(username);
    	for(String path : xpaths)
    	{
    		System.out.println(path);
    		ThreadController.restartNotifyList(path);
    		System.out.println("The path being restarted :" + path);
    		logA.doLog("UserController" , "[U-Controller]Thread has been restarted/updated for the xpath : " + path, "Info");
    		res++;
    	}
    	return res;
    	
    }
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void subscribeUserToStoredDataviews(String username) throws IOException
    {
    	ArrayList<String> xpaths = new ArrayList<String>();
    	User current = UserController.userObjects.get(username);
    	xpaths = DatabaseController.getUserDataviewList(username);
    	for(String path : xpaths)
    	{
    		System.out.println(path);
    		current.ammendCustomDV(path.trim());
    		ThreadController.addToNotifyList(path.trim(), username);
    	}
    	
    }
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static String logout(String username, String android_id)
	{
		int deviceCheck = UserController.userObjects.get(username).removeDevice(android_id);
		DatabaseController.logoutDevice(android_id);
		logA.doLog("UserController" , "[U-Controller]The status of the device check for" + username + " is :" + deviceCheck , "Info");
		//System.out.println("The status of the device check is :" + deviceCheck);
		if (deviceCheck == 1) {
			//System.out.println("User removed");
			removeUserFromDataviews(username);
			UserController.userObjects.remove(username);
			logA.doLog("UserController" , "[U-Controller]" + username + " has been successfully logged out." , "Info");
		} else {
			//System.out.println("Device removed");
			logA.doLog("UserController" , "[U-Controller]" + android_id + " from the user " + username + " has been successfully logged out." , "Info");
			refreshDeviceToStoredDataviews(username);
		}
		return "This device has been successfully logged out";
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void removeUserFromDataviews(String username) {
		ArrayList<String> xpaths = new ArrayList<String>();
		xpaths = DatabaseController.getUserDataviewList(username);
		for (String path : xpaths) {
			System.out.println(path);
			ThreadController.removeUserFromNotifyList(username, path);
			logA.doLog("UserController" , "[U-Controller]" + path + "  has been restarted for the user " + username , "Info");
		}

	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static String setCustomDV(String xpath, String userName) throws IOException
	{
		String ret = "";

		if(!UserController.userObjects.containsKey(userName)) // Capture for if the user exists in the system, can't add to a user that does not exist
		{
			logA.doLog("UserController" , "[U-Controller]Attempted access by invalid and potentially harmful user utilising username : " + userName , "Critical");
			return "You are not a valid user";
		}
		User current = UserController.userObjects.get(userName);

		ret = current.ammendCustomDV(xpath);		// Returns either a result as seen in the comparison below, or xpath or previous custom DV in entity that user is subscribed too. Returned xpath used for updating.

		if(ret.equals("Add Successful"))
		ThreadController.addToNotifyList(xpath, userName);	
		return ret;
		}

	public static void removeDataview(String xpath, String userName) {
		ThreadController.removeUserFromNotifyList(userName, xpath);
		userObjects.get(userName).removeDV(xpath);
		// TransmissionHandler.removeMessage(userName, xpath);
	
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
