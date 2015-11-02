package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
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


public class pushtestTest {
	pushtest test = new pushtest();
	String testXpath = "/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"CPU_Entity\")]/sampler[(@name=\"CPU_sampler\")][(@type=\"\")]/dataview[(@name=\"CPU_sampler\")]/rows/row[(@name=\"cpu_0\")]/cell[(@column=\"percentUtilisation\")]";

	@Test
	public void testPostCreation() throws IOException 
	{
		assertTrue(!test.postCreation().equals(null));
	}
	
	@Test
	public void testCreateJSONWithValidMessage() throws JSONException
	{
		assertEquals("Should equal first argument", "testing", new JSONObject(test.createJSON("testing", "a", "a", "a")).getJSONObject("data").getString("message"));
	}
	
	@Test
	public void testCreateJSONWithValidErrorID() throws JSONException
	{
		assertEquals("Should equal argument", testXpath , new JSONObject(test.createJSON("testing", testXpath, "a", "a")).getJSONObject("data").getString("xpath"));
	}
	
	@Test
	public void testCreateJSONWithValidValue() throws JSONException
	{
		assertEquals("Should equal argument", "result", new JSONObject(test.createJSON("testing", testXpath, "result", "a")).getJSONObject("data").getString("value"));
	}
	
	@Test
	public void testCreateJSONWithValidSeverity() throws JSONException
	{
		assertEquals("Should equal argument", "OK", new JSONObject(test.createJSON("testing", testXpath, "result", "OK")).getJSONObject("data").getString("severity"));
	}
	
	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_reason_ExpectRuntimeException() throws JSONException
	{
		 new JSONObject(test.createJSON(null,"","",""));
	}
	
	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_errorID_ExpectRuntimeException() throws JSONException
	{
		 new JSONObject(test.createJSON("",null,"",""));
	}
	
	
	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_value_ExpectRuntimeException() throws JSONException
	{
		 new JSONObject(test.createJSON("","",null,""));
	}
	
	
	@Test(expected = RuntimeException.class)
	public void testCreateJSONWithNullValue_severity_ExpectRuntimeException() throws JSONException
	{
		 new JSONObject(test.createJSON("","","",null));
	}
	
	/*@Test
	public void testCreateJSONWithNullValue_severity_ExpectRuntimeException1() throws JSONException
	{
		 new JSONObject(test.createJSON("","","",null));
	}*/
	
	
	
}
