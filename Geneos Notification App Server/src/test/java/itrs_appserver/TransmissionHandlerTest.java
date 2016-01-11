package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Test;

public class TransmissionHandlerTest {

	@Test
	public void testPostCreation() throws IOException {
		assertTrue(!TransmissionHandler.postCreation().equals(null));
	}
	
	@Test(expected = IOException.class)
	public void testFailedTransmission() throws JSONException, IOException{
		TransmissionHandler.sendPost("something", 1);
	}

}
