package geneos_notification.controllers;

import static com.itrsgroup.openaccess.common.Severity.CRITICAL;
import static com.itrsgroup.openaccess.common.Severity.WARNING;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.springframework.web.bind.annotation.RequestParam;

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

import geneos_notification.controllers.ThreadController.MyAnalysis;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import geneos_notification.objects.ThreadItem;
import geneos_notification.thread_operations.ThreadInstance;

public class ThreadController {

	static LtA logA = new LogObject();
	public static Map<String, ThreadItem> monitoringThreadList = new HashMap<String, ThreadItem>();
	public static ExecutorService executor = Executors.newFixedThreadPool(200);

	public static class MyAnalysis implements Callable<Long> {

		private String xpath;

		public MyAnalysis(String path) {
			this.xpath = path;
		}

		@Override
		public Long call() throws InterruptedException {
			logA.doLog("Thread", "[T-INFO]Initialization of thread for xpath : " + xpath, "Info");
			Long x = (long) 1;
			ThreadInstance.startSample(xpath);
			return x;
		}
	}

	public static void restartNotifyList(String xpath) {
		ThreadItem current = monitoringThreadList.get(xpath);
		current.getFuture().cancel(true);
		current.resetDevices();
		Callable<Long> worker = new ThreadController.MyAnalysis(xpath);
		Future<Long> thread = executor.submit(worker);
		current.setFuture(thread);
	}

	public static void addToNotifyList(String xpath, String userName) throws IOException {
		if (monitoringThreadList.containsKey(xpath)) {
			ThreadItem current = monitoringThreadList.get(xpath);
			current.addUserID(userName);

			current.getFuture().cancel(true);
			Callable<Long> worker = new ThreadController.MyAnalysis(xpath);
			Future<Long> thread = executor.submit(worker);
			current.setFuture(thread);

		}

		else {
			System.out.println("This is the thread xpath : " + xpath);

			Callable<Long> worker = new ThreadController.MyAnalysis(xpath);
			Future<Long> thread = executor.submit(worker);
			monitoringThreadList.put(xpath, new ThreadItem(xpath, thread, userName));

		}
		// TransmissionHandler.additionMessage(userName, xpath);
	}

	public void removeDataview(String entity, String xpath, String userName) {
		UserController.removeUserFromNotifyList(userName, xpath);
		UserController.userObjects.get(userName).removeDV(entity, xpath);
		// TransmissionHandler.removeMessage(userName, xpath);

	}

	public void editDV(String rentity, String aentity, String rxpath, String axpath, String userName)
			throws IOException {
		UserController.removeUserFromNotifyList(userName, rxpath);
		UserController.userObjects.get(userName).removeDV(rentity, rxpath);
		// TransmissionHandler.removeMessage(userName, rxpath);
		UserController.setCustomDV(aentity, axpath, userName);

	}

	public static void startPerpetualThread(String xpath) {
		Callable<Long> worker = new ThreadController.MyAnalysis(xpath);
		Future<Long> thread = executor.submit(worker);
		monitoringThreadList.get(xpath).setFuture(thread);
	}

}