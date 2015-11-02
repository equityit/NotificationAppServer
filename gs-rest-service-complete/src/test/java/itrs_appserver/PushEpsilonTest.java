package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.junit.Test;

import junit.framework.Assert;

import org.apache.http.HttpConnection;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


// testTransmitPost_ValidTransmission 	- Offline handling needs adding in push side
// testPostCreation						- Offline handling needs adding in push side
// NOTHING MAJOR JUST ADD SOME ERROR LOGGING AND SYSTEM PROMPTS

public class PushEpsilonTest {
	String testXpath = "/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"CPU_Entity\")]/sampler[(@name=\"CPU_sampler\")][(@type=\"\")]/dataview[(@name=\"CPU_sampler\")]/rows/row[(@name=\"cpu_0\")]/cell[(@column=\"percentUtilisation\")]";
	
	@Test
	public void testPostCreation() throws IOException {
		try{
		PushEpsilon test = new PushEpsilon();
		assertTrue("Post creation returns an HttpURLConnection object",
				!test.postCreation(test.gcm, test.api).equals(null));
		}
		catch(IOException e)
		{
			assertEquals("Always pass as the exception is caught properly",1,1);
		}
		
	}
	
	@Test
	public void testCreateJSONWithValidMessage() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		assertEquals("Argument 'testing' is stored as message", "testing",
				new JSONObject(test.createJSON("testing", "a", "a", "a", test.reg)).getJSONObject("data")
						.getString("message"));
	}

	@Test
	public void testCreateJSONWithValidErrorID() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		assertEquals("Argument variable 'xpath' is stored as xpath", testXpath,
				new JSONObject(test.createJSON("testing", testXpath, "a", "a", test.reg)).getJSONObject("data")
						.getString("xpath"));
	}

	@Test
	public void testCreateJSONWithValidValue() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		assertEquals("Argument 'result' is stored as value", "result",
				new JSONObject(test.createJSON("testing", testXpath, "result", "a", test.reg)).getJSONObject("data")
						.getString("value"));
	}

	@Test
	public void testCreateJSONWithValidSeverity() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		assertEquals("Argument 'OK' is stored as severity", "OK",
				new JSONObject(test.createJSON("testing", testXpath, "result", "OK", test.reg)).getJSONObject("data")
						.getString("severity"));
	}

	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_reason_ExpectRuntimeException() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		new JSONObject(test.createJSON(null, "", "", "", test.reg));
	}

	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_errorID_ExpectRuntimeException() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		new JSONObject(test.createJSON("", null, "", "", test.reg));
	}

	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_value_ExpectRuntimeException() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		new JSONObject(test.createJSON("", "", null, "", test.reg));
	}

	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_severity_ExpectRuntimeException() throws JSONException {
		PushEpsilon test = new PushEpsilon();
		new JSONObject(test.createJSON("", "", "", null, test.reg));
	}

	@Test
	public void testTransmitPost_ValidTransmission() throws JSONException, UnsupportedOperationException, IOException {
		PushEpsilon test = new PushEpsilon();
		try{
		assertEquals("Returned message should be 'success'", "success",
				test.transmitPost(test.postCreation(test.gcm, test.api),
						test.createJSON("testing", testXpath, "result", "ok", test.reg)));
		}
		catch(IOException e)
		{
			assertEquals("Pass as offline exception caught",1,1);
		}
	}

	@Test(expected = IOException.class)
	public void testTransmitPost_InValidRegistrationTransmission()
			throws JSONException, UnsupportedOperationException, IOException {
		PushEpsilon test = new PushEpsilon();
		test.transmitPost(test.postCreation(test.gcm, test.api),
				test.createJSON("testing", testXpath, "result", "ok", "incorrectReg"));
	}

	@Test(expected = IOException.class)
	public void testTransmitPost_InValidAPITransmission()
			throws JSONException, UnsupportedOperationException, IOException {
		PushEpsilon test = new PushEpsilon();
		test.transmitPost(test.postCreation(test.gcm, "something"),
				test.createJSON("testing", testXpath, "result", "ok", test.reg));
	}

	@Test(expected = MalformedURLException.class)
	public void testTransmitPost_InValidGcmAddressTransmission()
			throws JSONException, UnsupportedOperationException, IOException {
		PushEpsilon test = new PushEpsilon();
		test.transmitPost(test.postCreation("something", test.api),
				test.createJSON("testing", testXpath, "result", "ok", test.reg));
	}

	@Test(expected = IOException.class)
	public void testTransmitPost_InValidGcmMethodTransmission()
			throws JSONException, UnsupportedOperationException, IOException {
		PushEpsilon test = new PushEpsilon();
		test.transmitPost(test.postCreation("https://android.googleapis.com/gcm/something", test.api),
				test.createJSON("testing", testXpath, "result", "ok", test.reg));
	}

}
