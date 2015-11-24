package itrs_appserver;

import java.util.ArrayList;
import com.itrsgroup.openaccess.Callback;
import com.itrsgroup.openaccess.Closable;
import com.itrsgroup.openaccess.Connection;
import com.itrsgroup.openaccess.ErrorCallback;
import com.itrsgroup.openaccess.OpenAccess;
import static com.itrsgroup.openaccess.common.Severity.*;
import com.itrsgroup.openaccess.dataset.DataSetChange;
import com.itrsgroup.openaccess.dataset.DataSet;
import com.itrsgroup.openaccess.dataset.DataSetItem;
import com.itrsgroup.openaccess.dataset.DataSetQuery;
import com.itrsgroup.openaccess.dataset.DataSetTracker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.json.JSONException;
import org.json.JSONStringer;


public class pushtest {

    // Decleration of lists, connection and Map variable to allow for use and retention across multiple methods.
    public static Connection conn;
    public static ArrayList<String> probesPaths;
    public static ArrayList<String> entitiesPaths;
    public static ArrayList<String> samplersPaths;
    public static ArrayList<String> dataViewsPaths;
    static Map<String, String> propertiesMap = new HashMap<String, String>();
    public static ArrayList<String> warningList = new ArrayList<String>();
    public static ArrayList<String> criticalList = new ArrayList<String>();
    public static ArrayList<String> snoozeList = new ArrayList<String>();
    public final static String reg = "efxeHwADczU:APA91bEydq9KDyUNEzq3x-pXBkWWpiq2ydEapZdoJUlDUAVfjy0NkFAxADeTDEaC_rVjUNPIhMP7Abg8pc6gkj9gjzPXje_BTWTMmyO-a22PTLNKMwFgz9FxvIYHezQGduwgQcQCO0WG";
    public final static String api = "key=AIzaSyB24chqHK6z1MQfs5NmAXUwUyS8e8KN40k";
    public final static String gcm = "https://android.googleapis.com/gcm/send";
    public static String dvPath = "/geneos/gateway[(@name=\"GW_TEST_50144\")]/directory/probe[(@name=\"SYSMON\")]/managedEntity[(@name=\"CPU_Entity\")]/sampler[(@name=\"CPU_sampler\")][(@type=\"\")]/dataview[(@name=\"CPU_sampler\")]/";// This is the other point that will need adapting, other than these the program doesn't need to be altered.
    private static DataSet dataSet;

    public static void startSample(String oaAddress) {
        conn = OpenAccess.connect(GreetingController.getOAkey()); // This is one of the two points that will need adaptation for other use
        runScan();
    }

    private static void runScan() {
        while (1 == 1) {
            run(dvPath); // CALL TESTING OF COLLECTED XPATHS WHICH IS INFINITE
            try {
                Thread.sleep(10000);    // SLEEP FOR 10 SECONDS BEFORE TESTING THE XPATH RESULTS TO PREVENT SPAMMING, CAN BE REDUCED TO REAL TIME IF REQUIRED AND CAN BE HANDLED
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void run(String dvPath) {
        try {
            System.out.println(dvPath);
            //dvPath + "/dataview/rows/cell"
            DataSetQuery query = DataSetQuery.create(dvPath + "/rows/row[wild(@name,\"*\")]/cell"); //Allows for the creation of XPaths that match the criteria for the specfied data by adding wildcards to stored XPaths.
            final DataSetTracker dataSetTracker = new DataSetTracker();
            final CountDownLatch cdl = new CountDownLatch(1); //Specified to allow for closure in case of "Timeout".

            Closable c = conn.execute(query,
                    new Callback<DataSetChange>() {
                        @Override
                        public void callback(final DataSetChange change) {
                            dataSet = dataSetTracker.update(change); // This "updates" the Dataset with the data in 'change' when the information needs updating (or in this case is empty)
                            cdl.countDown();
                        }
                    },
                    new ErrorCallback() {
                        @Override
                        public void error(final Exception exception) {
                            System.err.println("Error retrieving DataSet: " + exception);
                        }
                    });

            try {
                cdl.await(10, SECONDS);
                c.close();
            } catch (InterruptedException e) {
                System.out.println("Status ------- RED");
                e.printStackTrace();
            }
            threadAnalysis();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Status ------- RED");
        }
    }

    private static void threadAnalysis() throws JSONException, IOException {
        System.out.println("//////////////////////////////// \nTHREAD SUCCESSFUL \n");
        alertChecker();
    }

    private static String alertDecider(String xpath, String severity) {
        String ret = "";
        ret = toCriticalMessage(severity, xpath, ret);
        ret = toWarningMessage(severity, xpath, ret);
        return ret;
    }

    private static String toWarningMessage(String severity, String xpath, String ret) {
        if (severity == "WARNING") {
            if (criticalList.contains(xpath)) {
                ret = "Critical to Warning Alert";
            } else {
                ret = "OK to Critical Alert";
            }
        }
        return ret;
    }

    private static String toCriticalMessage(String severity, String xpath, String ret) {
        if (severity == "CRITICAL") {
            if (warningList.contains(xpath)) {
                ret = "Warning to Critical Alert";
            } else {
                ret = "OK to Critical Alert";
            }
        }
        return ret;
    }

    private static void alertChecker() throws JSONException, IOException {
        int crtiticalAlertCounter = 0;
        int warningAlertCounter = 0;
        int snoozeAlertCounter = 0;
        for (DataSetItem item : dataSet.getItems()) // Loop through all the datasetitems within the data set by specifying in extended for (look for datasetitems until dataset.getitems() returns null)
		{

			if (item.getSeverity() == CRITICAL) {

				System.out.println("CRITICAL DATA ITEM NAME : " + item.getName() + "\nCRITICAL DATA ITEM PATH : "
						+ item.getPath() + "\n");
				crtiticalAlertCounter++;
				criticalHandler(item);
			} else if (criticalList.contains(item.getPath())) {
				criticalList.remove(item.getPath());
			}

			if (item.getSeverity() == WARNING) {

				System.out.println("WARNING DATA ITEM NAME : " + item.getName() + "\nWARNING DATA ITEM PATH : "
						+ item.getPath() + "\n");
				warningAlertCounter++;
				warningHandler(item);
			} else if (warningList.contains(item.getPath())) {
				warningList.remove(item.getPath());
			}

			if (item.isSnoozed()) {

				System.out.println("SNOOZED DATA ITEM NAME : " + item.getName() + "\nSNOOZED DATA ITEM PATH : "
						+ item.getPath() + "\n");
				snoozeAlertCounter++;
				snoozeHandler(item);
			} else if (snoozeList.contains(item.getPath())) {
				snoozeList.remove(item.getPath());
			}
		}

		if (crtiticalAlertCounter == 0) {
			System.out.println("NO CRITICAL DATA ITEMS");
		} else {
			crtiticalAlertCounter = 0;
		}

		if (warningAlertCounter == 0) {
			System.out.println("NO SNOOZED DATA ITEMS");
		} else {
			warningAlertCounter = 0;
		}

		if (snoozeAlertCounter == 0) {
			System.out.println("NO SNOOZED DATA ITEMS");
		} else {
			snoozeAlertCounter = 0;
		}
	}

	private static void snoozeHandler(DataSetItem item) throws JSONException, IOException {
		if (snoozeList.contains(item.getPath())) {
			System.out.println("Alert already exists");
		} else {
			snoozeList.add(item.getPath());
			sendPost("Snooze alert", item.getPath(), item.getValue(), item.getSeverity().toString());
		}
	}

	private static void warningHandler(DataSetItem item) throws JSONException, IOException {
		if (warningList.contains(item.getPath())) {
			System.out.println("Alert already exists");
		} else {
			String message = alertDecider(item.getPath(), "WARNING");
			warningList.add(item.getPath());
			sendPost(message, item.getPath(), item.getValue(), item.getSeverity().toString());
		}
	}

	private static void criticalHandler(DataSetItem item) throws JSONException, IOException {
		if (criticalList.contains(item.getPath())) {
			System.out.println("Alert already exists");
		} else {
			String message = alertDecider(item.getPath(), "CRITICAL");
			criticalList.add(item.getPath());
			sendPost(message, item.getPath(), item.getValue(), item.getSeverity().toString());
		}
	}

    public static void sendPost(String reason, String errorID, String value, String severity) throws JSONException, IOException {
        HttpURLConnection con = postCreation();
        String myString = createJSON(reason, errorID, value, severity);
        System.out.println("TRIGGERING ITEM : " + myString);
        transmitPost(con, myString);
    }
    
    private static void transmitPost(HttpURLConnection con, String myString) throws UnsupportedOperationException {
        try {
        	OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(myString);
            writer.flush();
            writer.close();
            
            InputStream is = con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String tester = response.toString();
            System.out.println("POST RETURN VALUE : " + tester);
            
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("success");
            } else {
                // Server returned HTTP error code.
            }
        } catch (MalformedURLException e) {
            System.out.println(e);
            System.out.println("url");
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("io");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String createJSON(String reason, String errorID, String value, String severity) throws JSONException {
    	
    	if(reason.equals(null))
    		throw new RuntimeException("JSON value reason found to be null");
    	if(errorID.equals(null))
    		throw new RuntimeException("JSON value errorID found to be null");
    	if(value.equals(null))
    		throw new RuntimeException("JSON value value found to be null");
    	if(severity.equals(null))
    		throw new RuntimeException("JSON value severity found to be null");
    	
        String myString = new JSONStringer()
                .object()
	                .key("to")
	                .value(reg)
	                .key("data")
	                .object()
		                .key("message")
		                .value(reason)
		                .key("xpath")
		                .value(errorID)
		                .key("value")
		                .value(value)
		                .key("severity")
		                .value(severity)
	                .endObject()
                .endObject()
                .toString();
        return myString;
    }

    public static HttpURLConnection postCreation() throws IOException {
    	URL obj = new URL(gcm);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setReadTimeout(10000);
        con.setConnectTimeout(15000);
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
		con.addRequestProperty("Authorization", api);
		con.addRequestProperty("Content-type", "application/json");
		con.connect();
        return con;
    }
}
