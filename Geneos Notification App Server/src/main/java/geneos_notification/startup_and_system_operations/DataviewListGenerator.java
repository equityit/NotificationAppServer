package geneos_notification.startup_and_system_operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.itrsgroup.openaccess.Callback;
import com.itrsgroup.openaccess.Closable;
import com.itrsgroup.openaccess.Connection;
import com.itrsgroup.openaccess.ErrorCallback;
import com.itrsgroup.openaccess.OpenAccess;
import com.itrsgroup.openaccess.dataset.DataSetChange;
import com.itrsgroup.openaccess.dataset.DataSet;
import com.itrsgroup.openaccess.dataset.DataSetItem;
import com.itrsgroup.openaccess.dataset.DataSetQuery;
import com.itrsgroup.openaccess.dataset.DataSetTracker;
import com.itrsgroup.openaccess.xpath.XPathBuilder;

import geneos_notification.controllers.InterfaceController;
import geneos_notification.controllers.ThreadController;
import geneos_notification.controllers.UserController;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.User;

import java.util.concurrent.CountDownLatch;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataviewListGenerator {

	public static Connection conn;
	public static ArrayList<String> probesPaths;
	public static ArrayList<String> entitiesPaths;
	public static ArrayList<String> samplersPaths;
	public static ArrayList<String> dataViewsPaths;
	public static Map<String, JSONObject> objects;
	public static ArrayList<String> list;
	static LtA logA = new LogObject();

	public static ArrayList<JSONObject> collectDataviews() throws JSONException {
		conn = OpenAccess.connect(InterfaceController.getOAkey());
		objects = new HashMap<String, JSONObject>();
		probesPaths = new ArrayList<String>();
		String basePath = "/geneos/gateway[wild(@name,\"*\")]/directory/probe";
		getDataSetItems(basePath, probesPaths, "");
		entitiesPaths = new ArrayList<String>();
		for (String probePath : probesPaths)
			getDataSetItems(probePath, entitiesPaths, "/managedEntity");
		samplersPaths = new ArrayList<String>();
		for (String entityPath : entitiesPaths)
			getDataSetItems(entityPath, samplersPaths, "/sampler");
		dataViewsPaths = new ArrayList<String>();
		for (String entityPath : samplersPaths)
			getDataSetItems(entityPath, dataViewsPaths, "/dataview");
		JSONObject dataviewList = new JSONObject();
		dataviewList.put("dataViews", new JSONArray(dataViewsPaths));
		list = new ArrayList<String>();
		JSONArray results = new JSONArray();
		results = dataviewList.getJSONArray("dataViews");

		if (results != null) {
			int len = results.length();
			for (int i = 0; i < len; i++) {
				list.add(results.get(i).toString());
			}
		}
		logA.doLog("Thread",
				"[DT-INFO]Collected Dataviews : " + list + " \nWith a length of " + list.toString().length(), "Info");
		conn.close();
		list = sortList(list);

		ArrayList<JSONObject> sender = new ArrayList<JSONObject>();

		for (int i = 0; i < list.size(); i++) {
			ThreadController.addToDataViewMontioringMap(list.get(i).trim(), objects.get(list.get(i)));
			sender.add(objects.get(list.get(i)));
		}
		System.out.println(sender.toString());
		return sender;
	}

	private static ArrayList<String> sortList(ArrayList<String> list) {
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> meOrder = orderMeList(list);
		Map<String, ArrayList<String>> organiser = mapAndOrderDataviews(list);
		for (String me : meOrder) {
			res.addAll(organiser.get(me));
		}
		return res;
	}

	private static Map<String, ArrayList<String>> mapAndOrderDataviews(ArrayList<String> list) {
		Map<String, ArrayList<String>> organiser = new HashMap<String, ArrayList<String>>();
		for (String extract : list) {
			String filter = getME(extract);
			if (organiser.containsKey(filter))
				organiser.get(filter).add(extract);
			else {
				ArrayList<String> dvS = new ArrayList<String>();
				dvS.add(extract);
				organiser.put(filter, dvS);
			}

		}

		for (Map.Entry<String, ArrayList<String>> entry : organiser.entrySet()) {
			Collections.sort(entry.getValue(), new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					s1 = getDV(s1);
					s2 = getDV(s2);
					return s1.compareToIgnoreCase(s2);
				}
			});
		}
		return organiser;
	}

	private static ArrayList<String> orderMeList(ArrayList<String> list) {
		ArrayList<String> meOrder = new ArrayList<String>();

		for (String extract : list) {
			extract = getME(extract);
			if (!meOrder.contains(extract))
				meOrder.add(extract);
		}

		Collections.sort(meOrder, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		return meOrder;
	}

	private static String getME(String path) {
		String res = null;
		String res1 = path.substring(path.lastIndexOf("managedEntity[(@name=\""), path.lastIndexOf("/sampler"));
		res = res1.substring(22, res1.lastIndexOf("\")"));
		return res;
	}

	private static String getDV(String path) {
		String filter = path.substring(path.lastIndexOf("dataview[(@name=\""));
		String ret = filter.substring(17, filter.lastIndexOf("\")"));
		return ret;
	}

	private static void getDataSetItems(final String path, final ArrayList<String> a, String append) {
		DataSetQuery query = DataSetQuery.create(path + append);
		final DataSetTracker dataSetTracker = new DataSetTracker();
		final CountDownLatch cdl = new CountDownLatch(1);

		Closable c = conn.execute(query, new Callback<DataSetChange>() {
			@Override
			public void callback(final DataSetChange change) {
				try {
					DataSet dataSet = dataSetTracker.update(change);
					if (!dataSet.getItems().isEmpty()) {
						cdl.countDown();
					}
					for (DataSetItem item : dataSet.getItems()) {
						a.add(item.getPath());
						JSONObject obj = new JSONObject();
						obj.put("XPath", item.getPath());
						obj.put("Snoozed", item.isSnoozed());
						obj.put("Severity", item.getSeverity().toString());
						objects.put(item.getPath(), obj);
					}
				} catch (JSONException e) {
					logA.doLog("Thread", "[DT-INFO]Error retrieving DataSet and translating to JSON : " + e.toString(),
							"Critical");
				}
			}
		},

				new ErrorCallback() {
					@Override
					public void error(final Exception exception) {
						logA.doLog("Thread", "[DT-INFO]Error retrieving DataSet: " + exception, "Critical");
					}
				});

		try {
			cdl.await(1, SECONDS);
			c.close();
		} catch (InterruptedException e) {
			logA.doLog("Thread", "[DT-INFO]Error retrieving DataSet: " + e.toString(), "Critical");
		}
	}
}
