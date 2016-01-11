package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itrsgroup.openaccess.OpenAccess;

import itrs_appserver.GreetingController.MyAnalysis;

public class AlertControllerTest {
	
	@BeforeClass
	public static void setup()
	{
		appUser holder = new appUser("test", "test", "test", 99999);
    	GreetingController.userObjects.put("test",holder);
    	AlertController.conn = OpenAccess.connect("geneos.cluster://192.168.220.54:2551?username=admin&password=admin"); 
    	Future<Long> thread = null;
    	GreetingController.monitoringThreadList.put("test", new NotificationList("/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"FKM\")]/sampler[(@name=\"FKM\")][(@type=\"\")]/dataview[(@name=\"FKM\")]", thread, "test"));
	}

	@Test(expected = RuntimeException.class)
	public void testRunWitnIncorrectXpath() {
		AlertController.run("incorrect");
	}
	
	@Test
	public void testAlertObjectCreatedCorrectly() //Must ensure path selected points to cell above OK severity
	{
		
		AlertController.run("/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"FKM\")]/sampler[(@name=\"FKM\")][(@type=\"\")]/dataview[(@name=\"FKM\")]");
		assertFalse(AlertController.alertList.isEmpty());
		//assertNotNull(AlertController.alertList.get("/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"FKM\")]/sampler[(@name=\"FKM\")][(@type=\"\")]/dataview[(@name=\"FKM\")]/rows/row[(@name=\"/home/gateway/ftmtest.txt\")]/cell[(@column=\"status\")]"));
	}

}
