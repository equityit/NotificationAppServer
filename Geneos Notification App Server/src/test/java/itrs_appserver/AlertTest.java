package itrs_appserver;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import geneos_notification.objects.Alert;

public class AlertTest {
	static Alert tester;

	@BeforeClass
	public static void setUp() throws JSONException
	{
	tester = new Alert("something","something","something","something", 1);
	}
	
	@Test
	public void testAlert() throws JSONException {
		assertTrue("Return an Alert Object",!(new Alert("something","something","something","something", 1)).equals(null));
	}

	@Test
	public void testUpdateAlert() throws JSONException {
		tester.updateAlert("something", "OK", "XPath", 1);
		assertEquals("After update Severity value is different, update has occurred", "OK", tester.getSeverity() );
	}
	
	@Test(expected = NullPointerException.class)
	public void testCreateAlert_FailureWithNullValue() throws JSONException {
		Alert testing = new Alert(null,null,null,null);
	}

}
