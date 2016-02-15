package geneos_notification.objects;


import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import geneos_notification.controllers.ThreadController;


public class Alert {

	private String JSON;
	private String xpath;
	private String message;
	private String value;
	private String severity;
	
	public Alert(String path, String val, String sev, String dvPath) throws JSONException {
		this.xpath = path;
		this.setSeverity(sev);
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
		internal.put("type", "alert");
		internal.put("time", LocalDateTime.now().toString());
		testingObj.put("data", internal);
		System.out.println(testingObj.toString());
		return testingObj.toString();
	}

	public  void updateMessage(String to) {
		message = (severity + " to " + to + " Alert");
	}

	public  String getJSON() {
		return JSON;
	}

	public  void setJSON(String jSON) {
		JSON = jSON;
	}

	public  String getXpath() {
		return xpath;
	}

	public  void setXpath(String path) {
		xpath = path;
	}

	public  String getMessage() {
		return message;
	}

	public  void setMessage(String mail) {
		message = mail;
	}

	public  String getValue() {
		return value;
	}

	public  void setValue(String val) {
		value = val;
	}

	public  String getSeverity() {
		return severity;
	}

	public  void setSeverity(String sev) {
		if (severity == null)
			message = ("New Alert :" + sev);
		else
			updateMessage(sev);
		severity = sev;
	}

}
