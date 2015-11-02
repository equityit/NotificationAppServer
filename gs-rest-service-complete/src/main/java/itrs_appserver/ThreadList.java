package itrs_appserver;

import java.util.ArrayList;

// Class is used to create objects specific to each monitoring thread, storing the user ID of all the subscribed users. Used for convenience
// Possibly replace User ID with registration key if applicable to cut out reference step

public class ThreadList {
	
	private final String xpath;
	private ArrayList<Integer> subscribedUserID = new ArrayList<Integer>();
	
	public ThreadList(String path)
	{
		this.xpath = path;
	}

}
