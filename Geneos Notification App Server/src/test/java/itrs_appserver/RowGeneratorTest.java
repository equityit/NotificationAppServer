package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.controllers.EmailController;
import geneos_notification.controllers.InterfaceController;
import geneos_notification.thread_operations.RowGenerator;

public class RowGeneratorTest {
	
	@BeforeClass
	public static void cSetup() throws InterruptedException, ExecutionException
	{
		InterfaceController.setKeyData("jdbc:mysql://localhost/test?user=root&password=iPods123", "geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
		EmailController.setDetails("smtp.hostedservice2.net", "HelpdeskAutomation@itrsgroup.com", "9AHNekkeJwUE7XD","192.168.10.128", "8443");
	}

	@Test
	public void getDataSetItemsTest() {
		assertTrue(RowGenerator.getRow("/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"CPU_Entity\")]/sampler[(@name=\"CPU_sampler\")][(@type=\"\")]/dataview[(@name=\"CPU_sampler\")]/rows/row[(@name=\"cpu_0\")]/cell[(@column=\"percentUtilisation\")]").size() > 1);
	}
	
	@Test(expected = RuntimeException.class)
	public void getDataSetItemsFailTest() {
		RowGenerator.getRow("fail' or 1 = 1");
	}
	
	@Test(expected = Exception.class)
	public void getDataSetItemsFailureTest() {
		RowGenerator.getRow("Nothing");
	}

}
