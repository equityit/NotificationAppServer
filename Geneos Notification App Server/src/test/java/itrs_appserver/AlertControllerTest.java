package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itrsgroup.openaccess.OpenAccess;

import geneos_notification.controllers.ThreadController.MyAnalysis;

public class AlertControllerTest {
	
/*	@BeforeClass
	public static void setup()
	{
		User holder = new User("test", "test", "test", 99999);
    	UserController.userObjects.put("test",holder);
    	ThreadController.conn = OpenAccess.connect("geneos.cluster://192.168.220.54:2551?username=admin&password=admin"); 
    	Future<Long> thread = null;
    	ThreadController.monitoringThreadList.put("test", new ThreadList("/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"FKM\")]/sampler[(@name=\"FKM\")][(@type=\"\")]/dataview[(@name=\"FKM\")]", thread, "test"));
	}

	@Test(expected = RuntimeException.class)
	public void testRunWitnIncorrectXpath() {
		ThreadController.run("incorrect");
	}
	
	@Test
	public void testAlertObjectCreatedCorrectly() //Must ensure path selected points to cell above OK severity
	{
		
		ThreadController.run("/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"FKM\")]/sampler[(@name=\"FKM\")][(@type=\"\")]/dataview[(@name=\"FKM\")]");
		assertFalse(ThreadController.alertList.isEmpty());
		//assertNotNull(AlertController.alertList.get("/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"FKM\")]/sampler[(@name=\"FKM\")][(@type=\"\")]/dataview[(@name=\"FKM\")]/rows/row[(@name=\"/home/gateway/ftmtest.txt\")]/cell[(@column=\"status\")]"));
	}
*/
}
