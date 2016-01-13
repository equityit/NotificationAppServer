package itrs_appserver;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class ThreadList {
	
	private  String xpath;
	private  ArrayList<String> monitoringUsers = new ArrayList<String>(); // Change to map with the user map Id as key storing only registration key
	private  Future<Long> thread;
	private	 regListItem reg;
	
	public ThreadList(String path, Future<Long> monitor, String userName)
	{
		this.xpath = path;
		this.thread = monitor;
		this.monitoringUsers.add(userName);
		this.reg = new regListItem(monitoringUsers);
	}
	
	public regListItem getRegList()
	{
		return this.reg;
	}
	
	public void setFuture(Future<Long> now)
	{
		this.thread = now;
	}
	
	public String getXpath()
	{
		return this.xpath;
	}
	
	public Future<Long> getFuture()
	{
		return this.thread;
	}
	
	public ArrayList<String> getUsers()
	{
		return this.monitoringUsers;
	}
	
	public int removeUser(String username)
	{
		if(monitoringUsers.size() == 1)
		{
			monitoringUsers.clear();
			return 1;
		}
		monitoringUsers.remove(username);
		this.reg = new regListItem(monitoringUsers);
/*		if (monitoringUsers.isEmpty())
			return 1;*/
		return 0;
	}
	
	public void resetDevices()
	{
		this.reg = new regListItem(monitoringUsers);
	}
	
	public void addUserID(String userName)
	{
		this.monitoringUsers.add(userName);
		this.reg = new regListItem(monitoringUsers);
	}
	
	public int removeUserID(String userName)
	{
		int ret = 0;
		this.monitoringUsers.remove(userName);
		if(this.monitoringUsers.size() == 0)
		{
			ret = 1;
		}
		this.reg = new regListItem(monitoringUsers);
		return ret;
	}
	
	public void closeMonitorthread(String path)
	{
		if(path.equals(this.xpath))
		{
			this.thread.cancel(true);
		}
	}

}