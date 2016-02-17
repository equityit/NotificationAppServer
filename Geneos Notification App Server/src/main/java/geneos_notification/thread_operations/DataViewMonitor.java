package geneos_notification.thread_operations;

import static com.itrsgroup.openaccess.common.Severity.CRITICAL;
import static com.itrsgroup.openaccess.common.Severity.WARNING;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import geneos_notification.startup_and_system_operations.DataviewListGenerator;

public class DataViewMonitor {
	public static LtA logA = new LogObject();
	public Connection conn;
	public Map<String, Alert> alertList = new HashMap<String, Alert>();
	private DataSet dataSet;
	private int firstRunSwitch = 1;
	private int sampleRate;
	
	public DataViewMonitor() throws InterruptedException
	{
		this.sampleRate = InterfaceController.sampleRate;
		startSample();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void startSample() throws InterruptedException {
		conn = OpenAccess.connect(InterfaceController.getOAkey());
		runScan();
	}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	private void runScan() throws InterruptedException {
		while (1 == 1) {
			run();
			try {
				Thread.sleep(sampleRate);
			} catch (InterruptedException ex) {
				logA.doLog("Thread", "[DVM-INFO]Thread internal termination confirmation", "Info");
				conn.close();
				return;
			}
		}
	}
	

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void run() {
		try {
			logA.doLog("Thread", "Thread xpath exection for path dataview monitoring!", "Info");
			DataSetQuery query = DataSetQuery.create("/geneos/gateway/directory/probe/managedEntity/sampler/dataview");
			final DataSetTracker dataSetTracker = new DataSetTracker();
			final CountDownLatch cdl = new CountDownLatch(1);
			dataSet = null;

			Closable c = conn.execute(query, new Callback<DataSetChange>() {
				@Override
				public void callback(final DataSetChange change) {
					dataSet = dataSetTracker.update(change);
					if (!dataSet.getItems().isEmpty()) {
						cdl.countDown();
					} 
				}
			}, new ErrorCallback() {
				@Override
				public void error(final Exception exception) {
					logA.doLog("Thread", "[DVM-ERROR]Error retrieving DataSet: " + exception, "Critical");
					cdl.countDown();
					throw new RuntimeException();
				}
			});

			try {
				cdl.await(1, SECONDS);
				c.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
				logA.doLog("Thread", "[DVM-ERROR]Thread cycle execution failed due to interrupt", "Critical");
				logA.doLog("Thread", e.toString(), "Critical");
				e.printStackTrace();
			}
			threadAnalysis();
		} catch (Exception e) {
			logA.doLog("Thread", "[DVM-ERROR]Thread cycle execution failed", "Critical");
			logA.doLog("Thread", e.toString(), "Critical");
			e.printStackTrace();
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void threadAnalysis() throws JSONException, IOException, InterruptedException, ExecutionException {
		logA.doLog("Thread", "[DVM-INFO]DV Monitoring Execution successful", "Info");
		alertChecker();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void sendNotificaiton(String xpath, String snoozeState) throws JSONException{
		if(ThreadController.monitoringThreadList.containsKey(xpath))
		{
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("registration_ids", new JSONArray(ThreadController.monitoringThreadList.get(xpath).getRegList().getRegList()));
		internal.put("Xpath", xpath);
		internal.put("snoozed", snoozeState);
		internal.put("type", "dvUpdate");
		testingObj.put("data", internal);
		System.out.println(testingObj.toString());
		TransmissionHandler.sendDVUpdate(testingObj.toString());
		}
	}
	
	private void sendSevNotificaiton(String xpath, String severityState) throws JSONException{
		if(ThreadController.monitoringThreadList.containsKey(xpath))
		{
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("registration_ids", new JSONArray(ThreadController.monitoringThreadList.get(xpath).getRegList().getRegList()));
		internal.put("Xpath", xpath);
		internal.put("Severity", severityState);
		internal.put("type", "dvUpdate");
		testingObj.put("data", internal);
		System.out.println(testingObj.toString());
		TransmissionHandler.sendDVUpdate(testingObj.toString());
		}
	}
	
	private void sendNotificaiton(String xpath, String snoozeState, String severity) throws JSONException{
		if(ThreadController.monitoringThreadList.containsKey(xpath))
		{
		JSONObject testingObj = new JSONObject();
		JSONObject internal = new JSONObject();
		testingObj.put("registration_ids", new JSONArray(ThreadController.monitoringThreadList.get(xpath).getRegList().getRegList()));
		internal.put("Xpath", xpath);
		internal.put("snoozed", snoozeState);
		internal.put("severity", severity);
		internal.put("type", "dvUpdate");
		testingObj.put("data", internal);
		System.out.println(testingObj.toString());
		TransmissionHandler.sendDVUpdate(testingObj.toString());
		}
	}
	
	private void alertChecker() throws JSONException, IOException, InterruptedException, ExecutionException {
		try {
			int change = 0;
			for (DataSetItem item : dataSet.getItems()) {
				int snoozeChangeSwitch = 0;
				int severityChangeSwitch = 0;
				String snoozeState = null;
				String severityChange = null;
				if (!ThreadController.dataViewMonitoringMap.containsKey(item.getPath().trim())) {
					change = 1;
					break;
				} else {
					int pos = DataviewListGenerator.list.indexOf(item.getPath());
					JSONObject obj = ThreadController.dataViewMonitoringMap.get(item.getPath().trim());
					if (String.valueOf(item.isSnoozed()).equals("true")
							&& String.valueOf(obj.get("Snoozed")).equals("false")) {
						obj.put("Snoozed", item.isSnoozed());
						InterfaceController.currentDataviewEntityList.get(pos).put("Snoozed", "true");
						logA.doLog("Thread", "[DVM-INFO]Snooze state change to snoozed for " + item.getName(), "Info");
						snoozeChangeSwitch = 1;
						snoozeState = "true";
					} else if (String.valueOf(item.isSnoozed()).equals("false")
							&& String.valueOf(obj.get("Snoozed")).equals("true")) {
						obj.put("Snoozed", item.isSnoozed());
						InterfaceController.currentDataviewEntityList.get(pos).put("Snoozed", "false");
						logA.doLog("Thread", "[DVM-INFO]Snooze state change to unsnoozed for " + item.getName(),
								"Info");
						snoozeChangeSwitch = 2;
						snoozeState = "false";
					}

					if (!item.getSeverity().toString().equals(obj.get("Severity"))) {
						obj.put("Severity", item.getSeverity().toString());
						InterfaceController.currentDataviewEntityList.get(pos).put("Severity",
								item.getSeverity().toString());
						severityChangeSwitch = 1;
						severityChange = item.getSeverity().toString();
					}

					if (snoozeChangeSwitch == 2 || snoozeChangeSwitch == 1) {
						sendNotificaiton(item.getPath(), snoozeState, item.getSeverity().toString());
					} else if (snoozeChangeSwitch == 1) {
						sendNotificaiton(item.getPath(), snoozeState);
					} else if (severityChangeSwitch == 1) {
						sendSevNotificaiton(item.getPath(), item.getSeverity().toString());
					}
				}
			}
			if (change == 1) {
				InterfaceController.updateDV();
				Thread.sleep(15000);
			}
			if (firstRunSwitch == 1)
				firstRunSwitch = 0;
		} catch (NullPointerException e) {
			if (firstRunSwitch == 1) {
				logA.doLog("Thread",
						"[DVM-WARNING]OpenAccess null response due to multiple reloads, this is handled and expected.",
						"Warning");
			} else {
				logA.doLog("Thread",
						"[DVM-ERROR]OpenAccess has produced no output and has not been resolved. Critical Issue.",
						"Critical");
			}
		}
	}
}