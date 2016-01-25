package geneos_notification.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.Alert;

public class TransmissionHandler {
	
    public static String api = "key=AIzaSyB24chqHK6z1MQfs5NmAXUwUyS8e8KN40k";
    public static String gcm = "https://android.googleapis.com/gcm/send";
    static LtA logA = new LogObject();
	
	
	public static int sendPost(Alert sendingAlert) {
		logA.doLog("Transmission", "[Transmission]Output JSON data : " + sendingAlert.getJSON(), "Info");
		try {
			HttpURLConnection con = postCreation();
			int res = transmitPost(con, sendingAlert.getJSON());
			return res;
		} catch (IOException e) {
			logA.doLog("Transmission",
					"[Transmission]An error was encountered with system connection parameters. Please ensure a stable and configured network configuration and try again.",
					"Critical");
			throw new RuntimeException(e);
		}
    }
    
    public static HttpURLConnection postCreation() throws IOException {
    	URL obj = new URL(gcm);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setReadTimeout(10000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
		con.addRequestProperty("Authorization", api);
		con.addRequestProperty("Content-type", "application/json");
		con.connect();
        return con;
    }
    
    public static int transmitPost(HttpURLConnection con, String myString) throws UnsupportedOperationException, IOException{
        int result = 1;
    	try {
        	OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(myString);
            writer.flush();
            writer.close();
            
            InputStream is = con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String tester = response.toString();
            logA.doLog("Transmission" , "[Transmission]POST Returned data : " + tester, "Info");
            
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	logA.doLog("Transmission" , "[Transmission]Transmission Successful", "Info");
                
            } else {
            	result = 0;
            }
        } catch (IOException e) {
        	logA.doLog("Transmission" , "[Transmission]IO error has been encountered, tranmission failed! : " + e.getMessage(), "Critical");
        	result = 0;
        	System.out.println(myString);
        	e.printStackTrace();
        	throw new IOException(e);
        } catch (Exception e) {
        	logA.doLog("Transmission" , "[Transmission]Undetermined error has been encountered, tranmission failed! : " + e.getMessage(), "Critical");
        	result = 0;
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }
		return result;
    }

}





/*  public static void additionMessage(String username, String xpath) throws IOException
{
	HttpURLConnection con = postCreation();
	transmitPost(con, createAJSON(username, xpath));
}

private static String createAJSON(String username, String xpath)
{
	JSONObject testingObj = new JSONObject();
	JSONObject internal = new JSONObject();
	testingObj.put("registration_ids",  new JSONArray(GreetingController.userObjects.get(username).getRegistrations()));
	internal.put("alteration", "addition");
	internal.put("xpath", xpath);
	testingObj.put("data", internal);
	return testingObj.toString();
}

public static void removeMessage(String username, String xpath) throws IOException
{
	HttpURLConnection con = postCreation();
	transmitPost(con, createRJSON(username, xpath));
}

private static String createRJSON(String username, String xpath)
{
	JSONObject testingObj = new JSONObject();
	JSONObject internal = new JSONObject();
	testingObj.put("registration_ids",  new JSONArray(GreetingController.userObjects.get(username).getRegistrations()));
	internal.put("alteration", "removal");
	internal.put("xpath", xpath);
	testingObj.put("data", internal);
	return testingObj.toString();
}*/

