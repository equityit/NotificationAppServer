package itrs_appserver;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import com.itrsgroup.openaccess.Connection;
import com.itrsgroup.openaccess.OpenAccess;

public class DataviewListGeneratorTest {
	
	DataviewListGenerator test = new DataviewListGenerator();

	@Before
	public void reSetOA()
	{
		test.setOaValue("geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
	}
	
	@Test
	public void testCollectDataviews_ProducesDataViewArray() throws JSONException {
		
		assertTrue("Does not return a blank array",!test.collectDataviews().toString().equals("[]"));
	}
	
	@Test
	public void testCollectDataviews_FaileDueToIncorrectOA() throws JSONException {
		test.setOaValue("geneos.cluster://192.168.220.54:2551?username=admin&password=bob");
		assertTrue("Will return a blank array",test.collectDataviews().toString().equals("[]"));
	}
	
	@Test
	public void testCollectDataviews_FaileDueToNoConnectionToOA() throws JSONException {
		test.setOaValue("geneos.cluster://192.168.220.59:2551?username=admin&password=admin");
		assertTrue("Will return a blank array",test.collectDataviews().toString().equals("[]"));
	}

}
