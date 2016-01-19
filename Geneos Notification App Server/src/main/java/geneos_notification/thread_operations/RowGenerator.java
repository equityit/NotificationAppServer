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
import com.itrsgroup.openaccess.dataset.DataSet;
import com.itrsgroup.openaccess.dataset.DataSetChange;
import com.itrsgroup.openaccess.dataset.DataSetItem;
import com.itrsgroup.openaccess.dataset.DataSetQuery;
import com.itrsgroup.openaccess.dataset.DataSetTracker;

import geneos_notification.controllers.InterfaceController;

public class RowGenerator {
	
	public static ArrayList<String> results;
	public static Connection conn;
	public static JSONObject testingObj;
	
	public static JSONObject getRow(String xpath)
	{
		conn = OpenAccess.connect(InterfaceController.getOAkey());
		testingObj = new JSONObject();
		String line = xpath;
		String path = line.substring(line.indexOf("/geneos"), line.lastIndexOf("/cell[(@column=")) + "/cell[wild(@column,\"*\")]";
		System.out.println(path);
		getDataSetItems(path);
		conn.close();
		return testingObj;
	}
	
	
	
	
	
	private static void getDataSetItems(final String path) {
		DataSetQuery query = DataSetQuery.create(path);
		final DataSetTracker dataSetTracker = new DataSetTracker();
		final CountDownLatch cdl = new CountDownLatch(1);

		Closable c = conn.execute(query, new Callback<DataSetChange>() {
			@Override
			public void callback(final DataSetChange change) {
				DataSet dataSet = dataSetTracker.update(change);
				for (DataSetItem item : dataSet.getItems()) {
					System.out.println(item);
					JSONObject internal = new JSONObject();
					internal.put("value", item.getValue());
					internal.put("severity", item.getSeverity());
					testingObj.put(item.getName(), internal);
				}
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
			System.out.println("Interrupted while waiting for updates");
			e.printStackTrace();
		}
	}

}
