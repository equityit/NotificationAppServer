package itrs_appserver;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

public class TransmissionHandler {
	
    public final static String api = "key=AIzaSyB24chqHK6z1MQfs5NmAXUwUyS8e8KN40k";
    public final static String gcm = "https://android.googleapis.com/gcm/send";
	
	
    public static void sendPost(Alert sendingAlert) throws JSONException, IOException {
        HttpURLConnection con = postCreation();
        System.out.println(sendingAlert.getJSON());
        transmitPost(con, sendingAlert.getJSON());
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
    
    private static void transmitPost(HttpURLConnection con, String myString) throws UnsupportedOperationException {
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
            System.out.println("POST RETURN VALUE : " + tester);
            
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("success");
            } else {
                // Server returned HTTP error code.
            }
        } catch (MalformedURLException e) {
            System.out.println(e);
            System.out.println("url");
        } catch (IOException e) {
        	e.printStackTrace();
            System.out.println(e);
            System.out.println("io");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
