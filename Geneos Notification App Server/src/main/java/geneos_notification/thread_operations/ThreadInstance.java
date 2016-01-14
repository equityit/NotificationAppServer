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

import geneos_notification.controllers.GreetingController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.TransmissionHandler;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.Alert;

public class ThreadInstance {
	static LtA logA = new LogObject();
	public static Connection conn;
	public static Map<String, Alert> alertList = new HashMap<String, Alert>();
	private static DataSet dataSet;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void startSample(String path) throws InterruptedException {
		conn = OpenAccess.connect(GreetingController.getOAkey());
		System.out.println(ThreadController.monitoringThreadList.get(path).getRegList().getRegList());
		runScan(path);
	}

/*public static ArrayList<String> getRegistrationList()
{
return registrationList;
}*/

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static void runScan(String xpath) throws InterruptedException {
		while (1 == 1) {
			run(xpath);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				System.out.println("Internal interrupt happens");
				logA.doLog("Thread", "[T-INFO]Thread internal termination confirmation", "Info");
				return;
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void run(String dvPath) {
		try {
			logA.doLog("Thread", "Thread xpath exection for path : " + dvPath, "Info");
			DataSetQuery query = DataSetQuery.create(dvPath + "/rows/row[wild(@name,\"*\")]/cell");
			final DataSetTracker dataSetTracker = new DataSetTracker();
			final CountDownLatch cdl = new CountDownLatch(1);

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
				logA.doLog("Thread", "[T-ERROR]Thread cycle execution failed", "Critical");
				logA.doLog("Thread", e.toString(), "Critical");
			}
			threadAnalysis(dvPath);
		} catch (Exception e) {
			logA.doLog("Thread", "[T-ERROR]Thread cycle execution failed", "Critical");
			logA.doLog("Thread", e.toString(), "Critical");
			throw new RuntimeException();
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static void threadAnalysis(String dvPath) throws JSONException, IOException {
		System.out.println("//////////////////////////////// \nTHREAD SUCCESSFUL \n");
		logA.doLog("Thread", "[T-INFO]Thread Execution successful", "Info");
		alertChecker(dvPath);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static void alertChecker(String dvPath) throws JSONException, IOException {
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

			} else if (isNewAlert(currentSeverity)) {
				alertList.put(item.getPath(),
						new Alert(item.getPath(), item.getValue(), item.getSeverity().toString(), dvPath));
				TransmissionHandler.sendPost(alertList.get(item.getPath()));
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
