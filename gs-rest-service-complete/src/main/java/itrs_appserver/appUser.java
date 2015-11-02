package itrs_appserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class appUser {
	
	private final String username;
	private Map<String,String> deviceList = new HashMap<String,String>();
	private ArrayList<CustomDataView> pathList = new ArrayList<CustomDataView>();

	public appUser(String uname, String a_id, String key, int id)
	{
		this.username = uname;
		deviceList.put(a_id,key);
	}
	
	public void addDevice(String a_id, String key)
	{
		deviceList.put(a_id,key);
	}
	
	public ArrayList<String> getRegistrations()
	{
		ArrayList<String> ret = new ArrayList<String>();
		for (Map.Entry<String, String> entry : deviceList.entrySet()) {
			ret.add(entry.getValue());
		}
		return ret;
	}
	
	public String getDeviceKey(String a_id)
	{
		return deviceList.get(a_id);
	}

	public String getUsername()
	{
		return username;
	}

	public String ammendCustomDV(String Entity, String Xpath)		// Searches the users custom data views by entity, if same entity is found it is updated, otherwise it is created.
	{
		String ret = "Add Successful";		
		int i = 0;
		for (CustomDataView test : pathList) {
			if (test.getXpath().equals(Xpath)) {		// Cycles through the dataview items to see if there is an entity match, if one is found the object is updated 
				i = 1;									// this allows only one custom dataview per entity as if one is found it is automaticallty updated
				ret = "This dataview is already being monitored by this user";				// If match found then the path for that entity is updated	
			}

		}
		if (i == 0)
		{ 											// If no entity with the name is found create a customer dataview for that entity
			pathList.add(new CustomDataView(Entity, Xpath));	// Add custom data view to user for that specific entity
			SQLControl.addCustomDataView(username, Entity, Xpath);
		}	
		return ret;	
	}
	
	// Need to add function to delete 

}
