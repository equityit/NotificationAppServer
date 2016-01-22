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
import geneos_notification.controllers.UserController;

public class InterfaceControllerTest {
	
	@BeforeClass
	public static void cSetup() throws InterruptedException, ExecutionException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD");
	}

	@Test
	public void loginTest() throws Exception {
		assertTrue(InterfaceController.login("testa5", "testa5", "testa5").equals("You are not authorised to use this app, please contact your local administrator"));
	}
	
	@Test
	public void logoutTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testb5@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testb5@Default'),'testb5','testb5', 12345, 1, 1)");
		InterfaceController.login("testb5@Default", "testb5", "testb5");
		assertTrue(InterfaceController.logout("testb5@Default", "testb5").equals("This device has been successfully logged out"));
	}
	
	@Test
	public void setCustomDVTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testc5@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testc5@Default'),'testc5','testc5', 12345, 1, 1)");
		InterfaceController.login("testc5@Default", "testc5", "testc5");
		assertTrue(InterfaceController.setCustomDV("testc5", "testc5@Default").equals("Add Successful"));
	}
	
	@Test
	public void removeDVTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testd5@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testd5@Default'),'testd5','testd5', 12345, 1, 1)");
		InterfaceController.login("testd5@Default", "testd5", "testd5");
		InterfaceController.setCustomDV("testd5", "testd5@Default");
		InterfaceController.removeDataview("testd5", "testd5@Default");
		assertTrue(!UserController.userObjects.get("testd5@Default").pathList.contains("testd5"));
	}
	
	@Test
	public void viewSystemKeysTest()
	{
		assertTrue(InterfaceController.viewSystemKeys().equals("jdbc:mysql://localhost/test?user=root&password=iPods123" + "  " + "geneos.cluster://192.168.220.54:2551?username=admin&password=admin"));
	}
	
	@Test
	public void verifyDeviceTest()
	{
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('teste5@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'teste5@Default'),'teste5','teste5', 12345, 0, 0)");
		assertTrue(InterfaceController.verifyDevice("test35", "12345").equals("<!DOCTYPE html><html><font face=\"interface,sans-serif\"><head><title>Geneos Notification App Device Registrations success</title></head><body><center><img src=\"https://www.itrsgroup.com/sites/all/themes/bootstrap_sub_theme/logo.png\" alt=\"logo.com\" width=\"100\" height=\"40.5\"><h1>Device subscribed successfully to Geneos Notification Server.</h1><p>Your device has been successfully registered to your account for the Geneos Notification App.</p><p>If you experience any problems connecting your device please contact your database administrator to verify the devices associated with your account. You will now be able to log into your Notification server with this device without any further authentication.</p><small><p>© ITRS 2015, ALL RIGHTS RESERVED - Created by Connor Morley & Daniel Ratnaras </font></center></body></html>"));
	}
	
	@Test
	public void getAllDataviewsTest() throws JSONException, InterruptedException, ExecutionException
	{
		InterfaceController.updateDV();
		InterfaceController.getAllDataview();
		assertTrue(!InterfaceController.currentDataviewEntityList.isEmpty());
		InterfaceController.currentDataviewEntityList = null;
	}
	@Test
	public void updateDVTest() throws JSONException, InterruptedException, ExecutionException
	{
		InterfaceController.updateDV();
		assertTrue(!InterfaceController.currentDataviewEntityList.isEmpty());
		InterfaceController.currentDataviewEntityList = null;
	}
	
	@AfterClass
	public static void cleanUp()
	{
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testb5@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testb5@Default')");
		DatabaseController.execCustom("delete from users where username like 'testb5@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testc5@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testc5@Default')");
		DatabaseController.execCustom("delete from users where username like 'testc5@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testd5@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testd5@Default')");
		DatabaseController.execCustom("delete from users where username like 'testd5@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'teste5@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'teste5@Default')");
		DatabaseController.execCustom("delete from users where username like 'teste5@Default'");
	}

}
