package geneos_notification.thread_operations;

import static com.itrsgroup.openaccess.common.Severity.CRITICAL;
import static com.itrsgroup.openaccess.common.Severity.WARNING;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.json.JSONException;

import com.itrsgroup.openaccess.Callback;
import com.itrsgroup.openaccess.Closable;
import com.itrsgroup.openaccess.Connection;
import com.itrsgroup.openaccess.ErrorCallback;
import com.itrsgroup.openaccess.OpenAccess;
import com.itrsgroup.openaccess.common.Severity;
import com.itrsgroup.openaccess.dataset.DataSet;
import com.itrsgroup.openaccess.dataset.DataSetChange;
import com.itrsgroup.openaccess.dataset.DataSetItem;
import com.itrsgroup.openaccess.dataset.DataSetQuery;
import com.itrsgroup.openaccess.dataset.DataSetTracker;

import geneos_notification.controllers.InterfaceController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.TransmissionHandler;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.Alert;

public class ThreadInstance {
	public static LtA logA = new LogObject();
	public Connection conn;
	public Map<String, Alert> alertList = new HashMap<String, Alert>();
	private DataSet dataSet;
	private int firstRunSwitch = 1;
	private int sampleRate;
	
	public ThreadInstance(String xpath) throws InterruptedException
	{
		this.sampleRate = InterfaceController.sampleRate;
		startSample(xpath);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void startSample(String path) throws InterruptedException {
		conn = OpenAccess.connect(InterfaceController.getOAkey());
		System.out.println(ThreadController.monitoringThreadList.get(path).getRegList().getRegList());
		runScan(path);
	}

/*public static ArrayList<String> getRegistrationList()
{
return registrationList;
}*/

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	private void runScan(String xpath) throws InterruptedException {
		while (1 == 1) {
			run(xpath);
			try {
				Thread.sleep(sampleRate);
			} catch (InterruptedException ex) {
				logA.doLog("Thread", "[T-INFO]Thread internal termination confirmation", "Info");
				conn.close();
				return;
			}
		}
	}
	

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void run(String dvPath) {
		try {
			logA.doLog("Thread", "Thread xpath exection for path : " + dvPath, "Info");
			DataSetQuery query = DataSetQuery.create(dvPath + "/rows/row[wild(@name,\"*\")]/cell");
			final DataSetTracker dataSetTracker = new DataSetTracker();
			final CountDownLatch cdl = new CountDownLatch(1);
			dataSet = null;

			Closable c = conn.execute(query, new Callback<DataSetChange>() {
				@Override
				public void callback(final DataSetChange change) {
					dataSet = dataSetTracker.update(change);
					if (!dataSet.getItems().isEmpty()) {
						cdl.countDown();
					}   ///// ALTERATION !!!!!!!!!
				}
			}, new ErrorCallback() {
				@Override
				public void error(final Exception exception) {
					logA.doLog("Thread", "[T-ERROR]Error retrieving DataSet: " + exception, "Critical");
					cdl.countDown();
					throw new RuntimeException();
				}
			});

			try {
				cdl.await(1, SECONDS);
				c.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
				logA.doLog("Thread", "[T-ERROR]Thread cycle execution failed due to interrupt", "Critical");
				logA.doLog("Thread", e.toString(), "Critical");
				e.printStackTrace();
			}
			threadAnalysis(dvPath);
		} catch (Exception e) {
			logA.doLog("Thread", "[T-ERROR]Thread cycle execution failed", "Critical");
			logA.doLog("Thread", e.toString(), "Critical");
			e.printStackTrace();
			//throw new RuntimeException();
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void threadAnalysis(String dvPath) throws JSONException, IOException {
		//System.out.println("//////////////////////////////// \nTHREAD SUCCESSFUL \n");
		logA.doLog("Thread", "[T-INFO]Thread Execution successful", "Info");
		alertChecker(dvPath);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void alertChecker(String dvPath) throws JSONException, IOException {
		try{
		for (DataSetItem item : dataSet.getItems()) {
			Severity currentSeverity = item.getSeverity();
			String currentXpath = item.getPath();

			if (alertList.containsKey(currentXpath)) {

				Alert updated = alertList.get(currentXpath);
				String currentSeverityString = item.getSeverity().toString();

				if (updated.getSeverity() != currentSeverityString) {
					if (isValidAlert(currentSeverity)) {
						updated.updateAlert(item.getValue(), currentSeverityString, dvPath);
					}
					TransmissionHandler.sendPost(alertList.get(currentXpath));
				}

			} else if (isNewAlert(currentSeverity) && firstRunSwitch == 0) {
				alertList.put(item.getPath(),
						new Alert(item.getPath(), item.getValue(), item.getSeverity().toString(), dvPath));
				TransmissionHandler.sendPost(alertList.get(item.getPath()));
			}
			else if(isNewAlert(currentSeverity))
			{
				alertList.put(item.getPath(),
						new Alert(item.getPath(), item.getValue(), item.getSeverity().toString(), dvPath));
			}
		}
		if(firstRunSwitch == 1)
			firstRunSwitch = 0;
		}
		catch(NullPointerException e)
		{
			if(firstRunSwitch == 1){
			logA.doLog("Thread", "[T-WARNING]OpenAccess null response due to multiple reloads, this is handled and expected.", "Warning");
			}
			else{
				logA.doLog("Thread", "[T-ERROR]OpenAccess has produced no output and has not been resolved. Critical Issue.", "Critical");
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static boolean isValidAlert(Severity item) {
		return item == Severity.OK || item == WARNING || item == CRITICAL;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static boolean isNewAlert(Severity item) {
		return item != Severity.OK && item != Severity.UNDEFINED;
	}
}
