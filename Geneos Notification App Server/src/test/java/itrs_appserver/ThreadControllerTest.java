package itrs_appserver;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.controllers.DatabaseController;
import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.InterfaceController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.UserController;

public class ThreadControllerTest {
	
	@BeforeClass
	public static void cSetup() throws InterruptedException, ExecutionException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD");
	}

	@Test
	public void callWithNewThreadTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testa3@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testa3@Default'),'testa3','testa3', 12345, 1, 1)");
		UserController.login("testa3@Default", "testa3", "testa3");
		ThreadController.addToNotifyList("testa3", "testa3@Default");
		assertTrue(ThreadController.monitoringThreadList.containsKey("testa3"));
	}
	
	@Test
	public void callWithAddTOExistingThreadTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testb3@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testb3@Default'),'testb3','testb3', 12345, 1, 1)");
		UserController.login("testb3@Default", "testb3", "testb3");
		ThreadController.addToNotifyList("testb3", "testb3@Default");
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testc3@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testc3@Default'),'testc3','testc3', 12345, 1, 1)");
		UserController.login("testc3@Default", "testc3", "testc3");
		ThreadController.addToNotifyList("testb3", "testc3@Default");
		assertTrue(ThreadController.monitoringThreadList.get("testb3").getUsers().contains("testb3@Default") && ThreadController.monitoringThreadList.get("testb3").getUsers().contains("testc3@Default"));
	}
	
	@Test
	public void editDVTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testd3@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testd3@Default'),'testd3','testd3', 12345, 1, 1)");
		UserController.login("testd3@Default", "testd3", "testd3");
		UserController.setCustomDV("testd3", "testd3@Default");
		ThreadController.editDV("testd3", "teste3", "testd3@Default");
		assertTrue(ThreadController.monitoringThreadList.get("teste3").getUsers().contains("testd3@Default"));
	}
	
	@Test
	public void removeUserFromNotifylistSingleUserTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testf3@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testf3@Default'),'testf3','testf3', 12345, 1, 1)");
		UserController.login("testf3@Default", "testf3", "testf3");
		ThreadController.addToNotifyList("testf3", "testf3@Default");
		ThreadController.removeUserFromNotifyList("testf3@Default", "testf3");
		assertTrue(!ThreadController.monitoringThreadList.containsKey("tetf3"));
	}
	@Test
	public void removeUserFromNotifylistTwoUsersTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testg3@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testg3@Default'),'testg3','testg3', 12345, 1, 1)");
		UserController.login("testg3@Default", "testg3", "testg3");
		ThreadController.addToNotifyList("testg3", "testg3@Default");
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testh3@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testh3@Default'),'testh3','testh3', 12345, 1, 1)");
		UserController.login("testh3@Default", "testh3", "testh3");
		ThreadController.addToNotifyList("testg3", "testh3@Default");
		ThreadController.removeUserFromNotifyList("testg3@Default", "testg3");
		assertTrue(ThreadController.monitoringThreadList.get("testg3").getUsers().contains("testh3@Default"));
	}
	
	
	
	@AfterClass
	public static void cleanUp()
	{
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testa3@Default')");
		DatabaseController.execCustom("delete from users where username like 'testa3@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testb3@Default')");
		DatabaseController.execCustom("delete from users where username like 'testb3@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testc3@Default')");
		DatabaseController.execCustom("delete from users where username like 'testc3@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testd3@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testd3@Default')");
		DatabaseController.execCustom("delete from users where username like 'testd3@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testf3@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testf3@Default')");
		DatabaseController.execCustom("delete from users where username like 'testf3@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testg3@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testg3@Default')");
		DatabaseController.execCustom("delete from users where username like 'testg3@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testh3@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testh3@Default')");
		DatabaseController.execCustom("delete from users where username like 'testh3@Default'");
	}

}
