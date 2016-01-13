package itrs_appserver;

import java.util.ArrayList;

public class regListItem {
	
	private final ArrayList<String> registrationList ;
	
	public regListItem(ArrayList<String> users)
	{
		this.registrationList = new ArrayList<String>(getREG(users));
	}
	
	public ArrayList<String> getRegList()
	{
		return registrationList;
	}
	
	private ArrayList<String> getREG(ArrayList<String> users)
	{
		ArrayList<String> ret = new ArrayList<String>();
		for (String username : users)
		{
			ArrayList<String> regs = UserController.userObjects.get(username).getRegistrations();
				for(String reg : regs)
				{
					ret.add(reg);
				}
		}
		return ret;
	}

}
