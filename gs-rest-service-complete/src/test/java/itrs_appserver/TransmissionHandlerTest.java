package itrs_appserver;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class TransmissionHandlerTest {

	@Test
	public void testPostCreation() throws IOException {
		assertTrue(!TransmissionHandler.postCreation().equals(null));
	}

}
