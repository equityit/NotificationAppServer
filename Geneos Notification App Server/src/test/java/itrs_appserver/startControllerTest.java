package itrs_appserver;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.controllers.DatabaseController;
import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.InterfaceController;
import geneos_notification.controllers.StartController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.UserController;

public class startControllerTest {
	
	@BeforeClass
	public static void cSetup() throws InterruptedException, ExecutionException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD");
	}
	
	@Test
	public void readSettingsFileTest() throws InterruptedException, ExecutionException {
		StartController.readSettingsFile();
		assertTrue(!StartController.setting.isEmpty());
	}
	
	@Test
	public void configureSettingsTest() throws InterruptedException, ExecutionException {
		StartController.readSettingsFile();
		StartController.configureSetting(StartController.setting);
		assertTrue(InterfaceController.getOAkey().equals("geneos.cluster://192.168.220.54:2551?username=admin&password=admin"));
	}
	
	@Test
	public void perpetualReloadTest() throws InterruptedException, ExecutionException {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testa2@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testa2@Default'),'testa2','testa2', 12345, 1, 1)");
		DatabaseController.execCustom("insert into user_paths(userid, xpath) values ((select userid from users where username like 'testa2@Default'), 'testa2')");
		StartController.perpetualReload();
		assertTrue(!ThreadController.monitoringThreadList.isEmpty() && !UserController.userObjects.isEmpty());
	}
	
	@Test(expected = RuntimeException.class)
	public void checkValidityFailTest() throws InterruptedException, ExecutionException {
	InterfaceController.setKeyData("nothing", "nothing");
    StartController.checkValidity();
	}
	
	@After
	public void reset()
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
	}
	
	@AfterClass
	public static void cleanUp()
	{
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testa2@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testa2@Default')");
		DatabaseController.execCustom("delete from users where username like 'testa2@Default'");
	}

}
