package itrs_appserver;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.controllers.DatabaseController;
import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.InterfaceController;
import geneos_notification.objects.Alert;

public class AlertTest {
	
	@BeforeClass
	public static void setup() throws JSONException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD", "192.168.10.128", "8443");
	}

/*	@Test
	public void createJSONTest() throws Exception {
		Alert test;
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testa6@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testa6@Default'),'testa6','testa6', 12345, 1, 1)");
		InterfaceController.login("testa6@Default", "testa6", "testa6");
		InterfaceController.setCustomDV("testa6", "testa6@Default");
		test = new Alert("testa6", "Something", "Critical", "testa6");
		assertTrue(test.createJSON("testa6").equals("{\"data\":{\"severity\":\"Critical\",\"xpath\":\"testa6\",\"message\":\"New Alert :Critical\",\"value\":\"Something\"},\"registration_ids\":[\"testa6\"]}"));
	}*/
	
/*	@Test
	public void updateMessageTest() throws Exception {
		Alert testa;
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testb6@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testb6@Default'),'testb6','testb6', 12345, 1, 1)");
		InterfaceController.login("testb6@Default", "testb6", "testb6");
		InterfaceController.setCustomDV("testb6", "testb6@Default");
		testa = new Alert("testb6", "Something", "Fail", "testb6");
		testa.updateMessage("pass");
		System.out.println(testa.getMessage());
		assertTrue(testa.getMessage().equals("Fail to pass Alert"));
	}*/
	
/*	@Test
	public void firstMessageTest() throws Exception {
		Alert testa;
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testc6@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testc6@Default'),'testc6','testc6', 12345, 1, 1)");
		InterfaceController.login("testc6@Default", "testc6", "testc6");
		InterfaceController.setCustomDV("testc6", "testc6@Default");
		testa = new Alert("testc6", "Something", "Fail", "testc6");
		assertTrue(testa.getMessage().equals("New Alert :Fail"));
	}*/
	
	
	@AfterClass
	public static void cleanUp()
	{
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testa6@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testa6@Default')");
		DatabaseController.execCustom("delete from users where username like 'testa6@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testb6@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testb6@Default')");
		DatabaseController.execCustom("delete from users where username like 'testb6@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testc6@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testc6@Default')");
		DatabaseController.execCustom("delete from users where username like 'testc6@Default'");
	}

}
