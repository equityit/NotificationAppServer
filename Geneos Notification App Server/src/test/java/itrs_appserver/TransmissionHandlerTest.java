package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.controllers.DatabaseController;
import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.InterfaceController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.TransmissionHandler;
import geneos_notification.controllers.UserController;
import geneos_notification.objects.Alert;

public class TransmissionHandlerTest {
	
	@BeforeClass
	public static void cSetup() throws InterruptedException, ExecutionException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		//EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD","192.168.10.128", "8443");
	}

	@Test
	public void testPostCreation() throws IOException {
		assertTrue(!TransmissionHandler.postCreation().equals(null));
	}
	
	@Test(expected = IOException.class)
	public void postCreationFailTest() throws IOException
	{
		TransmissionHandler.gcm = "nothing"; 
		TransmissionHandler.postCreation();
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
	
	@Test(expected = IOException.class)
	public void testTransmissionFail() throws IOException
	{
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("fail", "fCPKIL7K5H0:APA91bER9gg86wQYCx1audS_IWFyR5fxB96E7JhKCDavAWy0nimqTri2tLWuxsbbdMHCwzuGsGjhVfra82zl_ASnpNEQF5PKr09KC9GMHmndFj2S6uA_VYOs84q5AQPa-seZJG7PkwYY");
		internal.put("message", "something");
		internal.put("xpath", "Something");
		internal.put("wrong", "Something");
		internal.put("severity", "something");
		internal.put("time", LocalDateTime.now().toString());
		testingObj.put("fail", internal);
		String output =  testingObj.toString();
		System.out.println(output);
		HttpURLConnection con = TransmissionHandler.postCreation();
		TransmissionHandler.transmitPost(con, output);
	}
	
	
	@Test
	public void sendPostTest() throws Exception
	{
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testa9@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testa9@Default'),'testa9','testa9', 12345, 1, 1)");
		UserController.login("testa9@Default", "testa9", "testa9");
		UserController.setCustomDV("testa9", "testa9@Default");
		Alert test = new Alert("testa9", "0", "bad", "testa9");
		assertTrue(TransmissionHandler.sendPost(test) == 1);
	}
	
	@Test(expected = RuntimeException.class)
	public void sendPostURLFailureTest() throws Exception
	{
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testa10@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testa10@Default'),'testa10','testa10', 12345, 1, 1)");
		UserController.login("testa10@Default", "testa10", "testa10");
		UserController.setCustomDV("testa10", "testa10@Default");
		Alert test = new Alert("testa10", "0", "bad", "testa10");
		TransmissionHandler.gcm = "nothing"; 
		TransmissionHandler.sendPost(test);
	}
	
	@After
	public void reset()
	{
		TransmissionHandler.gcm = "https://android.googleapis.com/gcm/send";
	}
	
	@AfterClass
	public static void cleanUp()
	{
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testa9@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testa9@Default')");
		DatabaseController.execCustom("delete from users where username like 'testa9@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testa10@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testa10@Default')");
		DatabaseController.execCustom("delete from users where username like 'testa10@Default'");
	}

}
