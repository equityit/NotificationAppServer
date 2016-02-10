package geneos_notification.thread_operations;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.simple.JSONObject;

import com.itrsgroup.openaccess.Callback;
import com.itrsgroup.openaccess.Closable;
import com.itrsgroup.openaccess.Connection;
import com.itrsgroup.openaccess.ErrorCallback;
import com.itrsgroup.openaccess.OpenAccess;
import com.itrsgroup.openaccess.commands.CommandExecuteQuery;
import com.itrsgroup.openaccess.commands.CommandExecutionChange;
import com.itrsgroup.openaccess.dataset.DataSet;
import com.itrsgroup.openaccess.dataset.DataSetChange;
import com.itrsgroup.openaccess.dataset.DataSetItem;
import com.itrsgroup.openaccess.dataset.DataSetQuery;
import com.itrsgroup.openaccess.dataset.DataSetTracker;

import geneos_notification.controllers.InterfaceController;

public class OACommander {
	
	static public Connection conn;
	static public String command;
	static String result = "";
	
/*	public CommandExecution(String targ, String com)
	{
		this.target = targ;
		if(com.equals("snooze"))
			command = "/SNOOZE:manualAllMe";
		if(com.equals("wake"))
			command = "/SNOOZE:unsnoozeAllMe";
	}*/
	
	public static String issueCommand(String xpath, String com, String user)
	{
		result = "";
		command = "";
		conn = OpenAccess.connect(InterfaceController.getOAkey());
		if(com.equals("snooze"))
			command = "/SNOOZE:manualAllMe";
		if(com.equals("unsnooze"))
			command = "/SNOOZE:unsnoozeAllMe";
		sendCommand(xpath, command, user, com);
		conn.close();
		return result;
	}
	
	
	
	
	
	private static void sendCommand(String path, String com, String user, String origCom) {
		final DataSetTracker dataSetTracker = new DataSetTracker();
		final CountDownLatch cdl = new CountDownLatch(1);
		
		
        CommandExecuteQuery execution = CommandExecuteQuery.create(path, com);
        execution.putArgument("User comment", "User " + user + " has " + origCom + " this dataItem.");

		 Closable c = conn.execute(execution,
	                new Callback<CommandExecutionChange>() {
	                    @Override
	                    public void callback(final CommandExecutionChange change) {
	                        switch (change.getStatus()) {
	                            case COMPLETE:
	                                System.out.println("Command execution complete");
	                                result = "success";
	                                cdl.countDown();
	                            case FAILURE:
	                                System.err.println("Command execution failed");
	                                System.err.println(change.getContent());
	                                result = "failure";
	                                cdl.countDown();
	                        }
	                    }

	                },
	                new ErrorCallback() {
	                    @Override
	                    public void error(final Exception exception) {
	                        System.err.println("Unable to execute command: " + exception);
	                        result = "critical";
	                        cdl.countDown();
	                    }
	                }
	        );

		try {
			cdl.await(10, SECONDS);
			c.close();
		} catch (InterruptedException e) {
			System.out.println("Interrupted while waiting for updates");
			e.printStackTrace();
		}
	}
	
}
