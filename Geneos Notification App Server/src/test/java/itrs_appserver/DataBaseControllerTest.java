package itrs_appserver;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;

import geneos_notification.controllers.DatabaseController;
import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.InterfaceController;
import geneos_notification.controllers.UserController;

public class DataBaseControllerTest {
	
	@BeforeClass
	public static void cSetup() throws InterruptedException, ExecutionException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		//EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD","192.168.10.128", "8443");
	}

	@Test
	public void verifyStoredDeviceTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testa1@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testa1@Default'),'testa1','testa1', 12345, 0, 0)");
		DatabaseController.verifyStoredDevice("testa1", "12345");
		assertTrue(UserController.login("testa1@Default", "testa1", "testa1").equals("successfully logged in"));
	}
	
	@Test
	public void removeCustomDataviewTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testb1@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testb1@Default'),'testb1','testb1', 12345, 0, 0)");
		DatabaseController.execCustom("insert into user_paths(userid, xpath) values ((select userid from users where username like 'testb1@Default'), 'testb1')");
		DatabaseController.removeCustomDataview("testb1@Default","testb1");
		assertTrue(UserController.refreshDeviceToStoredDataviews("testb1@Default") == 0);
	}
	
	@Test
	public void getUserDataviewListTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testc1@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testc1@Default'),'testc1','testc1', 12345, 0, 0)");
		DatabaseController.execCustom("insert into user_paths(userid, xpath) values ((select userid from users where username like 'testc1@Default'), 'testc1')");
		assertTrue(DatabaseController.getUserDataviewList("testc1@Default").get(0).equals("testc1"));
	}
	
	@Test
	public void getLiveDevicesTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('testd1@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'testd1@Default'),'testd1','testd1', 12345, 0, 0)");
		assertTrue(!DatabaseController.getLiveDevices().isEmpty());
	}
	
	@Test
	public void getLivePathsTest() throws Exception {
		DatabaseController.execCustom("insert into users(username, domainID, created_date) values ('teste1@Default', 1, now());");
		DatabaseController.execCustom("insert into devices(userid, android_id, registration_key, verification_code, active, loggedin) values((select userid from users where username like 'teste1@Default'),'teste1','teste1', 12345, 0, 0)");
		DatabaseController.execCustom("insert into user_paths(userid, xpath) values ((select userid from users where username like 'teste1@Default'), 'teste1')");
		assertTrue(!DatabaseController.getLivePaths("'teste1@Default'").get("teste1").isEmpty());
	}
	
	@Test(expected = RuntimeException.class)
	public void sqlConnectFailTest()
	{
		DatabaseController.setAddress("nothing");
		DatabaseController.SQLConnect();
	}
	
	@Test(expected = RuntimeException.class)
	public void userCheckFailTest()
	{
		DatabaseController.checkUser("fail' or 1 = 1", "fail");
	}
	
	@Test(expected = RuntimeException.class)
	public void checkValidDomainFailTest() throws SQLException
	{
		DatabaseController.checkValidDomain("fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void createUserFailTest() throws Exception
	{
		DatabaseController.createUser("fail' or 1 = 1", "fail' or 1 = 1", "fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void createInValidDeviceFailTest() throws Exception
	{
		DatabaseController.createInValidDevice("fail' or 1 = 1", "fail' or 1 = 1", "fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void checkDeviceExistenceFailTest() throws Exception
	{
		DatabaseController.checkDeviceExistence("fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void addCustomDataViewFailTest() throws Exception
	{
		DatabaseController.addCustomDataView("fail' or 1 = 1","fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void verifyStoredDeviceFailTest() throws Exception
	{
		DatabaseController.verifyStoredDevice("fail' or 1 = 1","fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void removeCustomDataviewFailTest() throws Exception
	{
		DatabaseController.removeCustomDataview("fail' or 1 = 1","fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void getUserDataviewListFailTest() throws Exception
	{
		DatabaseController.getUserDataviewList("fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void logoutDeviceFailTest() throws Exception
	{
		DatabaseController.logoutDevice("fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void loginDeviceFailTest() throws Exception
	{
		DatabaseController.loginDevice("fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void getLivePathsFailTest() throws Exception
	{
		DatabaseController.getLivePaths("fail' or 1 = 1");
	}
	
	@Test(expected = RuntimeException.class)
	public void execCustomFailTest() throws Exception
	{
		DatabaseController.execCustom("fail' or 1 = 1");
	}
	
	@After
	public void reset()
	{
		DatabaseController.setAddress("jdbc:mysql://localhost/test?user=root&password=iPods123");
	}
	
	@AfterClass
	public static void cleanUp()
	{
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testa1@Default')");
		DatabaseController.execCustom("delete from users where username like 'testa1@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testb1@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testb1@Default')");
		DatabaseController.execCustom("delete from users where username like 'testb1@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testc1@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'testc1@Default')");
		DatabaseController.execCustom("delete from users where username like 'testc1@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'testd1@Default')");
		DatabaseController.execCustom("delete from users where username like 'testd1@Default'");
		DatabaseController.execCustom("delete from devices where userid = (select userid from users where username like 'teste1@Default')");
		DatabaseController.execCustom("delete from user_paths where userid = (select userid from users where username like 'teste1@Default')");
		DatabaseController.execCustom("delete from users where username like 'teste1@Default'");
	}

}
