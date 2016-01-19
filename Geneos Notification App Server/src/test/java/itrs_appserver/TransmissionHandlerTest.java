package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.TransmissionHandler;

public class TransmissionHandlerTest {

	@Test
	public void testPostCreation() throws IOException {
		assertTrue(!TransmissionHandler.postCreation().equals(null));
	}
	
	@Test(expected = IOException.class)
	public void testFailedTransmission() throws JSONException, IOException{
		TransmissionHandler.sendPost("something", 1);
	}
	
	@Test
	public void testTransmissionCorrect() throws IOException
	{
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("to", "fCPKIL7K5H0:APA91bER9gg86wQYCx1audS_IWFyR5fxB96E7JhKCDavAWy0nimqTri2tLWuxsbbdMHCwzuGsGjhVfra82zl_ASnpNEQF5PKr09KC9GMHmndFj2S6uA_VYOs84q5AQPa-seZJG7PkwYY");
		internal.put("message", "something");
		internal.put("xpath", "Something");
		internal.put("value", "Something");
		internal.put("severity", "something");
		internal.put("time", LocalDateTime.now().toString());
		testingObj.put("data", internal);
		String output =  testingObj.toString();
		System.out.println(output);
		HttpURLConnection con = TransmissionHandler.postCreation();
		assertEquals(1,TransmissionHandler.transmitPost(con, output));
		
	}

}
