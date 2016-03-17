package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.controllers.DatabaseController;
import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.InterfaceController;
import geneos_notification.controllers.StartController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.UserController;
import geneos_notification.objects.CustomDataView;
import geneos_notification.objects.User;

public class UserControllerTest {
	
	@BeforeClass
	public static void cSetup() throws InterruptedException, ExecutionException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD","192.168.10.128", "8443");
	}

	@Test
	public void loginUnauthorisedDomainTest() throws Exception {
		assertTrue("You are not authorised to use this app, please contact your local administrator".equals(UserController.login("something@wrong", "test", "test")));
	}
	
	@Test
	public void loginAuthotrisedDomainFirstTimeTest() throws Exception {
		assertTrue(UserController.login("test@Default", "test", "test").equals("Since you are a new user you will need to verify your device. We have sent you an email, please follow the contained URL to verify your device"));
	}
	
	@Test
	public void loginAuthotrisedDomainSecondTimeWithoutVeriticationTest() throws Exception {
		UserController.login("testa@Default", "testa", "testa");
		assertTrue(UserController.login("testa@Default", "testa", "testa").equals("This device has already been registered, please check your email for a verification URL"));
	}
	
	@Test
	public void loginAuthotrisedDomainSecondTimeWithSecondDeviceTest() throws Exception {
		UserController.login("testb@Default", "testb", "testb");
		assertTrue(UserController.login("testb@Default", "testz", "testz").equals("We have sent you a device authorisation email, please follow the URL provided to verify this device"));
	}
	
	@Test
	public void loginAuthotrisedUserWithFirstDeviceTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testc@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testc@Default'),'testc','testc', 12345, 1, 1)"); 
		assertTrue(UserController.login("testc@Default", "testc", "testc").equals("successfully logged in"));
	}
	
	@Test
	public void loginAuthotrisedUserWithSecondDeviceTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testd@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testd@Default'),'testd','testd', 12345, 1, 1)");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testd@Default'),'testx','testx', 12345, 1, 1)");
		UserController.login("testd@Default", "testd", "testd");
		assertTrue(UserController.login("testd@Default", "testx", "testx").equals("successfully logged in"));
	}
	
	@Test
	public void checkLoggedInStatusTest() throws Exception {
		UserController.userObjects.put("something", new User("something", "abc", "abc"));
		UserController.userObjects.get("something").pathList.add(new CustomDataView("test"));
		assertTrue(UserController.checkLoggedInStatus("something", "abc") == 1);
	}
	
	@Test
	public void setCustomDVTestSucceedTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('teste@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'teste@Default'),'teste','teste', 12345, 1, 1)");
		UserController.login("teste@Default", "teste", "teste");
		assertTrue(UserController.setCustomDV("test", "teste@Default").equals("Add Successful"));
	}
	
	@Test
	public void setCustomDVTestFailTest() throws Exception {
		assertTrue(UserController.setCustomDV("test", "testFail@Default").equals("You are not a valid user"));
	}
	
	@Test
	public void logoutTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testf@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testf@Default'),'testf','testf', 12345, 1, 1)");
		UserController.login("testf@Default", "testf", "testf");
		assertTrue(UserController.logout("testf@Default", "testf").equals("This device has been successfully logged out"));
	}
	
	@Test
	public void loginFailureCatchTest() throws Exception {
		assertTrue(UserController.login("fail' or 1 = 1", "fail' or 1 = 1", "fail' or 1 = 1").equals("An error occured - Please contact your administrator"));
	}
	
	
	
	@AfterClass
	public static void cleanUp()
	{
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'test@Default')");
		DatabaseController.execCustom("delete from users where username like 'test@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testa@Default')");
		DatabaseController.execCustom("delete from users where username like 'testa@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testb@Default')");
		DatabaseController.execCustom("delete from users where username like 'testb@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testc@Default')");
		DatabaseController.execCustom("delete from users where username like 'testc@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testd@Default')");
		DatabaseController.execCustom("delete from users where username like 'testd@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'teste@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'teste@Default')");
		DatabaseController.execCustom("delete from users where username like 'teste@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testf@Default')");
		DatabaseController.execCustom("delete from users where username like 'testf@Default'");
	}
	

}
