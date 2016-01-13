package itrs_appserver;


import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;


public class Alert {

	private static String JSON;
	private static String xpath;
	private static String message;
	private static String value;
	private static String severity;
	
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
	
	public String createJSON(String dvPath) throws JSONException{
		
		if (message.equals(null) || xpath.equals(null) || value.equals(null) || severity.equals(null))
			throw new RuntimeException("Value submitted to JSON constrcutor found to be null");
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("registration_ids", new JSONArray(ThreadController.monitoringThreadList.get(dvPath).getRegList().getRegList()));
		internal.put("message", message);
		internal.put("xpath", xpath);
		internal.put("value", value);
		internal.put("severity", severity);
		internal.put("time", LocalDateTime.now().toString());
		testingObj.put("data", internal);
		return testingObj.toString();
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
	
	public Alert(String path, String val, String sev, String dvPath, int test) throws JSONException {
		this.xpath = path;
		setSeverity(sev);
		this.value = val;
		this.JSON = (dvPath);
	}
	
	public void updateAlert(String val, String sev, String dvPath, int test) throws JSONException {
		setSeverity(sev);
		value = val;
		JSON = dvPath;
	}

}
