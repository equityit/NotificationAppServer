package itrs_appserver;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;


public class Alert {

	private static String JSON;
	private static String xpath;
	private static String message;
	private static String value;
	private static String severity;
	// public final static String reg = "e5vmAu78ghQ:APA91bEhmmo-DA7EXo4alOc5sCKYRk6_6fwfROYRMtr7zt-LsZDUd-OxGfhv9BvyX103ySs9lKM1oz29B1POJFvzMQCMCm8316ftWruhqk1_6XlEzgZxMSVbB6uzNtsslBnqEgtM8p0S";
	//public final static String reg = "dIfSzoEpuMo:APA91bHn07Bt_ZlFSgI-VMfs8n3qvzG1RM3TfqA7wNk9VsMNR_bSOLRhp3fjrR_owxyhkk5htkq50clvjQmAl6Cjb4cOn-FXd8X0c8dN2PT8xXSrEfhDiihI8yJLDUYOVeAMal-PiZX8";
	//public static String[] regList = {"cilBhu-L2Z0:APA91bGMj8ybkmvYiSfI4L_JkNbmc4BHIn7Lf2yysk2Mt6HmpolFvBNMFwIxIX6b7TsF1ZQuUpguI-6N6DTSSbfAwzpGiKRL8tQnsGAiUOXCvNVP0J2vqMakkgO34SXlwmpEnXZy8nBZ", "dIfSzoEpuMo:APA91bHn07Bt_ZlFSgI-VMfs8n3qvzG1RM3TfqA7wNk9VsMNR_bSOLRhp3fjrR_owxyhkk5htkq50clvjQmAl6Cjb4cOn-FXd8X0c8dN2PT8xXSrEfhDiihI8yJLDUYOVeAMal-PiZX8"};
	//public static ArrayList<String> reglist = new ArrayList<String>();
	//public static JSONObject reglisting = new JSONObject(regList);
	
	public Alert(String path, String val, String sev, String dvPath) throws JSONException {
		this.xpath = path;
		setSeverity(sev);
		this.value = val;
		this.JSON = createJSON(dvPath);
	}

	public void updateAlert(String val, String sev, String dvPath) throws JSONException {
		setSeverity(sev);
		value = val;
		JSON = createJSON(dvPath);
	}

/*	public static void arraySet()
	{
		NotificationList nowList = GreetingController.getNotificationList(xpath);
		ArrayList<String> users = nowList.getUsers();
		for(String user : users)
		{
			appUser now = GreetingController.userObjects.get(user);
			ArrayList<String> registrations = now.getRegistrations();
			for(String reg : registrations)
			{
				reglist.add(reg);
			}
			
		}
	}*/
	
	
	
	public String createJSON(String dvPath) throws JSONException{
		
		if (message.equals(null) || xpath.equals(null) || value.equals(null) || severity.equals(null))
			throw new RuntimeException("Value submitted to JSON constrcutor found to be null");
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("registration_ids", new JSONArray(GreetingController.monitoringThreadList.get(dvPath).getRegList().getRegList()));
		internal.put("message", message);
		internal.put("xpath", xpath);
		internal.put("value", value);
		internal.put("severity", severity);
		testingObj.put("data", internal);
		return testingObj.toString();
	}
	
	/*public static String createJSON() throws JSONException{
		try{
		if (message.equals(null) || xpath.equals(null) || value.equals(null) || severity.equals(null))
			throw new RuntimeException("Value submitted to JSON constrcutor found to be null");

		String myString = new JSONStringer().object().key("to").value(reg).key("data").object().key("message")
				.value(message).key("xpath").value(xpath).key("value").value(value).key("severity").value(severity)
				.endObject().endObject().toString();
		return myString;
		}
		catch (JSONException e)
		{
			throw new JSONException(e);
		}
	}*/

	private static void updateMessage(String from, String to) {
		message = (severity + " to " + to + " Alert");
	}

	public static String getJSON() {
		return JSON;
	}

	public static void setJSON(String jSON) {
		JSON = jSON;
	}

	public static String getXpath() {
		return xpath;
	}

	public static void setXpath(String path) {
		xpath = path;
	}

	public static String getMessage() {
		return message;
	}

	public static void setMessage(String mail) {
		message = mail;
	}

	public static String getValue() {
		return value;
	}

	public static void setValue(String val) {
		value = val;
	}

	public static String getSeverity() {
		return severity;
	}

	public static void setSeverity(String sev) {
		if (severity == null)
			message = ("New Alert :" + sev);
		else
			updateMessage(severity, sev);
		severity = sev;
	}

}


/*
package itrs_appserver;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.core.net.Severity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.json.simple.JSONObject;


public class Alert {

	private static String JSON;
	private static String xpath;
	private static String message;
	private static String value;
	private static String severity;
	// public final static String reg = "e5vmAu78ghQ:APA91bEhmmo-DA7EXo4alOc5sCKYRk6_6fwfROYRMtr7zt-LsZDUd-OxGfhv9BvyX103ySs9lKM1oz29B1POJFvzMQCMCm8316ftWruhqk1_6XlEzgZxMSVbB6uzNtsslBnqEgtM8p0S";
	public final static String reg = "dIfSzoEpuMo:APA91bHn07Bt_ZlFSgI-VMfs8n3qvzG1RM3TfqA7wNk9VsMNR_bSOLRhp3fjrR_owxyhkk5htkq50clvjQmAl6Cjb4cOn-FXd8X0c8dN2PT8xXSrEfhDiihI8yJLDUYOVeAMal-PiZX8";
	public static String[] regList = {"cilBhu-L2Z0:APA91bGMj8ybkmvYiSfI4L_JkNbmc4BHIn7Lf2yysk2Mt6HmpolFvBNMFwIxIX6b7TsF1ZQuUpguI-6N6DTSSbfAwzpGiKRL8tQnsGAiUOXCvNVP0J2vqMakkgO34SXlwmpEnXZy8nBZ", "dIfSzoEpuMo:APA91bHn07Bt_ZlFSgI-VMfs8n3qvzG1RM3TfqA7wNk9VsMNR_bSOLRhp3fjrR_owxyhkk5htkq50clvjQmAl6Cjb4cOn-FXd8X0c8dN2PT8xXSrEfhDiihI8yJLDUYOVeAMal-PiZX8"};
	//public static ArrayList<String> regAlist = new ArrayList<String>();
	//public static JSONObject reglisting = new JSONObject(regList);
	
	public Alert(String path, String val, String sev) throws JSONException {
		this.xpath = path;
		setSeverity(sev);
		this.value = val;
		this.JSON = createJSON(true);
	}

	public void updateAlert(String val, String sev) throws JSONException {
		setSeverity(sev);
		value = val;
		JSON = createJSON(true);
	}

	public void arraySet()
	{
		regAlist.add("dIfSzoEpuMo:APA91bHn07Bt_ZlFSgI-VMfs8n3qvzG1RM3TfqA7wNk9VsMNR_bSOLRhp3fjrR_owxyhkk5htkq50clvjQmAl6Cjb4cOn-FXd8X0c8dN2PT8xXSrEfhDiihI8yJLDUYOVeAMal-PiZX8");
		regAlist.add("e5vmAu78ghQ:APA91bEhmmo-DA7EXo4alOc5sCKYRk6_6fwfROYRMtr7zt-LsZDUd-OxGfhv9BvyX103ySs9lKM1oz29B1POJFvzMQCMCm8316ftWruhqk1_6XlEzgZxMSVbB6uzNtsslBnqEgtM8p0S");
	}
	
	public static String createJSON(Boolean x) throws JSONException{
		
		if (message.equals(null) || xpath.equals(null) || value.equals(null) || severity.equals(null))
			throw new RuntimeException("Value submitted to JSON constrcutor found to be null");
		try{
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("registration_ids", new JSONArray(regList));
		internal.put("message", message);
		internal.put("xpath", xpath);
		internal.put("value", value);
		internal.put("severity", severity);
		testingObj.put("data", internal);
		return testingObj.toString();
		}
		catch (JSONException e)
		{
			throw new JSONException(e);
		}
		
	}
	
	public static String createJSON() throws JSONException{
		try{
		if (message.equals(null) || xpath.equals(null) || value.equals(null) || severity.equals(null))
			throw new RuntimeException("Value submitted to JSON constrcutor found to be null");

		String myString = new JSONStringer().object().key("to").value(reg).key("data").object().key("message")
				.value(message).key("xpath").value(xpath).key("value").value(value).key("severity").value(severity)
				.endObject().endObject().toString();
		return myString;
		}
		catch (JSONException e)
		{
			throw new JSONException(e);
		}
	}

	private static void updateMessage(String from, String to) {
		message = (severity + " to " + to + " Alert");
	}

	public static String getJSON() {
		return JSON;
	}

	public static void setJSON(String jSON) {
		JSON = jSON;
	}

	public static String getXpath() {
		return xpath;
	}

	public static void setXpath(String path) {
		xpath = path;
	}

	public static String getMessage() {
		return message;
	}

	public static void setMessage(String mail) {
		message = mail;
	}

	public static String getValue() {
		return value;
	}

	public static void setValue(String val) {
		value = val;
	}

	public static String getSeverity() {
		return severity;
	}

	public static void setSeverity(String sev) {
		if (severity == null)
			message = ("New Alert :" + sev);
		else
			updateMessage(severity, sev);
		severity = sev;
	}

}


*/






