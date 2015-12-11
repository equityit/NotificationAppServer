package itrs_appserver;

import static com.itrsgroup.openaccess.common.Severity.CRITICAL;
import static com.itrsgroup.openaccess.common.Severity.WARNING;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.ArrayList;
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

public class AlertController {
	
		public static Connection conn;
	    public static Map<String,Alert> alertList = new HashMap<String,Alert>();
	    private static DataSet dataSet;
	    public static ArrayList<String> registrationList = new ArrayList<String>();
	    
	  
	public static void startSample(String path) throws InterruptedException {
	   conn = OpenAccess.connect(GreetingController.getOAkey()); // This is one of the two points that will need adaptation for other use
	   System.out.println(GreetingController.monitoringThreadList.get(path).getRegList().getRegList());
	   runScan(path);
	}
	
	public static ArrayList<String> getRegistrationList()
	{
		return registrationList;
	}

	private static void runScan(String xpath) throws InterruptedException {
	   while (1 == 1) {
	       run(xpath); // CALL TESTING OF COLLECTED XPATHS WHICH IS INFINITE
	       try {
              Thread.sleep(10000);    // SLEEP FOR 10 SECONDS BEFORE TESTING THE XPATH RESULTS TO PREVENT SPAMMING, CAN BE REDUCED TO REAL TIME IF REQUIRED AND CAN BE HANDLED
	            } catch (InterruptedException ex) {
	            	System.out.println("Internal interrupt happens");
	                return;
	            }
	        }
	}

	public static void run(String dvPath) {
		try {
			System.out.println(dvPath);
			DataSetQuery query = DataSetQuery.create(dvPath + "/rows/row[wild(@name,\"*\")]/cell");
			final DataSetTracker dataSetTracker = new DataSetTracker();
			final CountDownLatch cdl = new CountDownLatch(1);

			Closable c = conn.execute(query, new Callback<DataSetChange>() {
				@Override
				public void callback(final DataSetChange change) {
					dataSet = dataSetTracker.update(change);
					cdl.countDown();
				}
			}, new ErrorCallback() {
				@Override
				public void error(final Exception exception) {
					System.err.println("Error retrieving DataSet: " + exception);
				}
			});

			try {
				cdl.await(5, SECONDS);
				c.close();
			} catch (InterruptedException e) {
				System.out.println("Status ------- RED");
				e.printStackTrace();
			}
			threadAnalysis(dvPath);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			System.out.println("Status ------- RED");
		}
	}

	private static void threadAnalysis(String dvPath) throws JSONException, IOException {
		System.out.println("//////////////////////////////// \nTHREAD SUCCESSFUL \n");
		alertChecker(dvPath);
	}

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

	private static boolean isValidAlert(Severity item) {
		return item == Severity.OK || item == WARNING || item == CRITICAL;
	}

	private static boolean isNewAlert(Severity item) {
		return item != Severity.OK && item != Severity.UNDEFINED;
	}
}