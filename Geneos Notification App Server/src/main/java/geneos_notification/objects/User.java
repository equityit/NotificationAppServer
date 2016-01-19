package geneos_notification.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import geneos_notification.controllers.DatabaseController;

public class User {

	private final String username;
	public Map<String, String> deviceList = new HashMap<String, String>();
	public ArrayList<CustomDataView> pathList = new ArrayList<CustomDataView>();

	public User(String uname, String a_id, String key) {
		this.username = uname;
		deviceList.put(a_id, key);
	}

	public void addDevice(String a_id, String key) {
		deviceList.put(a_id, key);
	}

	public int removeDevice(String android_id) {
		if (deviceList.size() == 1) {
			deviceList.clear();
			return 1;
		}
		deviceList.remove(android_id);
		System.out.println(deviceList);
		return 0;
	}

	public ArrayList<String> getRegistrations() {
		ArrayList<String> ret = new ArrayList<String>();
		for (Map.Entry<String, String> entry : deviceList.entrySet()) {
			ret.add(entry.getValue());
		}
		return ret;
	}

	public String getDeviceKey(String a_id) {
		return deviceList.get(a_id);
	}

	public String getUsername() {
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
			DatabaseController.addCustomDataView(username, Entity, Xpath);
		}	
		return ret;	
	}
	
	// Need to add function to delete 
	
	public void removeDV(String entity, String xpath)
	{
		int i = 0;
		CustomDataView removal = null;
		for (CustomDataView test : pathList) {
			if (test.getXpath().equals(xpath)) {		// Cycles through the dataview items to see if there is an entity match, if one is found the object is updated 
				System.out.println(test);
				DatabaseController.removeCustomDataview(username, entity, xpath);
				removal = test;
				i = 1;
				break;
			}
		}
		if (i == 1)
			pathList.remove(removal);
		if (i == 0)
			System.out.println("There was a serious error as a xpath was specified not linked to the user account which should be impossible");
	}

}
