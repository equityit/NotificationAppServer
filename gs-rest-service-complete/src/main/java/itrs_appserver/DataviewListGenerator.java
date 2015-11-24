package itrs_appserver;

import java.util.ArrayList;
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
import java.util.concurrent.CountDownLatch;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataviewListGenerator {

	public static Connection conn = OpenAccess
			.connect("geneos.cluster://192.168.220.54:2551?username=admin&password=admin");
	public static ArrayList<String> probesPaths;
	public static ArrayList<String> entitiesPaths;
	public static ArrayList<String> samplersPaths;
	public static ArrayList<String> dataViewsPaths;
	private static DataSet dataSet;

/*	public static void main(String[] args) throws JSONException {
		collectDataviews();
	}*/

	public static ArrayList<String> collectDataviews() throws JSONException {
		ExecutorService executor = Executors.newFixedThreadPool(50);

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

		ArrayList<String> list = new ArrayList<String>();

		JSONArray results = new JSONArray();

		results = dataviewList.getJSONArray("dataViews");

		if (results != null) {
			int len = results.length();
			for (int i = 0; i < len; i++) {
				list.add(results.get(i).toString());
			}
		}

		System.out.println(dataviewList);
		System.out.println(XPathBuilder.xpath().entity("Something").get());
		System.out.println(list);
		System.out.println(list.toString().length());
		return list;

	}

	private static void getDataSetItems(final String path, final ArrayList<String> a, String append) {
		DataSetQuery query = DataSetQuery.create(path + append);
		final DataSetTracker dataSetTracker = new DataSetTracker();
		final CountDownLatch cdl = new CountDownLatch(1);

		Closable c = conn.execute(query, new Callback<DataSetChange>() {
			@Override
			public void callback(final DataSetChange change) {
				DataSet dataSet = dataSetTracker.update(change);
				for (DataSetItem item : dataSet.getItems()) {
					a.add(item.getPath());
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

	public void setOaValue(String oa) 
	{
		conn = OpenAccess.connect(oa);
	}
}
