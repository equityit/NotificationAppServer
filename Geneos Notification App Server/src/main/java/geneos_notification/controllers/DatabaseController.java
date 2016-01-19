package geneos_notification.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;

public class DatabaseController {
	
  private static String address = null;
  private static Connection conn = null;
  private static Statement stmt = null;
  private static ResultSet res = null;
  private static ResultSet res1 = null;
  public static Random random = new Random(System.currentTimeMillis());
  static LtA logA = new LogObject();

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////
  
  public static void SQLConnect(){
    try {
      // This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      // Setup the connection with the DB
      conn = DriverManager
          .getConnection(address);
    }
    catch(Exception e)
    {
    	logA.doLog("SQL" , "[SQL]Connection information issue either driver or address : " + e.toString(), "Critical");
        //System.out.println(e);
        throw new RuntimeException();
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////
  
@SuppressWarnings("finally")
public static int checkUser(String username, String android_id) {
		SQLConnect();
		int result = 0;
		int queryOut = 0;
		logA.doLog("SQL" , "[SQL]Checking user : " + username + " & " + android_id, "Info");
		//System.out.println(username + "    " + android_id);
		try {

			stmt = conn.createStatement();
			res = stmt.executeQuery("select userId from users where username like '" + username + "'");

			if (!res.isBeforeFirst()) 
			{
				close();
				return result; 				 // user name does not already exist in the database, return identifier for fail = 0
			}

			else 
			{
				queryOut = extractUserID(queryOut);
				result = validDeviceCheck(result, queryOut, android_id); // Will return either 1 for invalid, or 2 for valid
			}

		} catch (Exception e) {
			logA.doLog("SQL" , "[SQL]SQL query issue was encountered causing SQL failure : " + e.toString(), "Critical");
			// System.out.println(e);
			throw new RuntimeException(e);
		} finally {
			close();
			return result;
		}

	}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

private static int validDeviceCheck(int result, int queryOut, String android_id) throws SQLException {	//CREATE STORED PROCEDURE
	stmt = conn.createStatement();
	res1 = stmt.executeQuery("select id from devices where userid = " + queryOut + " and active = 1 and android_id = '" + android_id + "'");
	if (!res1.isBeforeFirst()) 
	{
		logA.doLog("SQL" , "[SQL]Device Check = User has yet to verify device : " + android_id, "Info");
		return 1;
	} 
	else 
	{
		result = 2;
	}
	return result;
}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

private static int extractUserID(int queryOut) throws SQLException {
	while (res.next()) 
	{
		int value = res.getInt(1);
		queryOut = value;
	}
	return queryOut;
}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

public static int checkValidDomain(String username) throws SQLException {
	SQLConnect();
	int ret;
	int queryOut = 0;
	try{
	stmt = conn.createStatement();
	res = stmt.executeQuery("call sp_Confirm_Valid_Domain('" + username +"')"); // Create query that extracts the domain from username and checks it against db white list, if correct then go into creation method
	if (!res.isBeforeFirst()) 
	{
		logA.doLog("SQL" , "[SQL]User has tried to log in with an invalid domain : " + username, "Warning");
		ret = 0;
	} 
	else
	{
		ret = 1;
	}
	close();
	return ret;
	}
	catch(Exception e)
	{
		logA.doLog("SQL" , "[SQL]Undetermined error was encountered causing query failure! : " + e.toString(), "Critical");
		e.printStackTrace();
		throw new RuntimeException(e);
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public static void createUser(String username, String android_id, String key) throws Exception
  {
		 SQLConnect();
		 
			try {
				stmt = conn.createStatement();
				int ran = random.nextInt();
				res = stmt.executeQuery("call sp_Create_New_User_With_Invalid_Device('" + username + "','" + android_id + "','" + key + "', "+ ran + ")");
				EmailController.sendMail(username, ran, android_id);
			} catch (SQLException e) {
				logA.doLog("SQL" , "[SQL]Query error while creating new user : " + username + " \nError is : " + e.toString(), "Critical");
				throw new RuntimeException(e);
			}
			finally{
				close();	
			}
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
  
 public static void createInValidDevice(String username, String android_id, String key) throws Exception
 {
	 SQLConnect();
	 
	try {
		stmt = conn.createStatement();
		int ran = random.nextInt();
		res = stmt.executeQuery("call sp_Create_Invalid_Device('" + username + "','" + android_id + "','" + key + "',"+ ran +")");
		EmailController.sendMail(username, ran, android_id);
	} catch (SQLException e) {
		logA.doLog("SQL" , "[SQL]Query error while creating invalid device " + android_id + " for user : " + username + " \nError is : " + e.toString(), "Critical");
		e.printStackTrace();
		throw new RuntimeException(e);
	}
	finally{
		close();	
	}
	
	 
 }
 
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 public static int checkDeviceExistence(String android_id)
 {
		try {
			SQLConnect();
			stmt = conn.createStatement();
			res = stmt.executeQuery("select * from devices where android_id = '" + android_id + "'");

			if (!res.isBeforeFirst()) {
				close();
				return 0;
			}
			close();
			return 1;
		} catch (SQLException e) {
			logA.doLog("SQL" , "[SQL]Query error while checkintg status of device : " + android_id + " \nError is : " + e.toString(), "Critical");
			throw new RuntimeException(e);
		}
 }
 
 
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void addCustomDataView(String username, String entity, String xpath) {
		SQLConnect();

		try {
			stmt = conn.createStatement();
			res = stmt
					.executeQuery("call sp_Add_Dataview_To_User('" + username + "','" + entity + "','" + xpath + "')");
		} catch (SQLException e) {
			logA.doLog("SQL" , "[SQL]Query error while adding dataview " + xpath + " to user : " + username + " \nError is : " + e.toString(), "Critical");
			throw new RuntimeException(e);
		} finally {
			close();
		}

	}
 
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////

public static void verifyStoredDevice(String android_id, String verification)
{
	SQLConnect();
	try {
	int random = Integer.parseInt(verification);
	stmt = conn.createStatement();
	res = stmt.executeQuery("call sp_verify_device('" + android_id + "'," + random + ")");
	} catch (SQLException e) {
	logA.doLog("SQL" , "[SQL]Error while conducting verification for device : " + android_id + " with verification code : "+ verification + " \nError is : " + e.toString(), "Critical");
	throw new RuntimeException(e);
	}
	finally{
	close();	
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////

public static void removeCustomDataview(String username, String entity, String xpath)
{
	SQLConnect();

	try {
		stmt = conn.createStatement();
		res = stmt
				.executeQuery("call sp_remove_dataview_from_user('" + username + "','" + xpath + "')");
	} catch (SQLException e) {
		logA.doLog("SQL" , "[SQL]Error while removing xpath " + xpath + " from user : " + username + " \nError is : " + e.toString(), "Critical");
		throw new RuntimeException(e);
	} finally {
		close();
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static ArrayList<String> getUserDataviewList(String username) {
		SQLConnect();
		ArrayList<String> resArray = new ArrayList<String>();
		try {
			stmt = conn.createStatement();
			res = stmt.executeQuery("call sp_Get_User_Dataviews('" + username + "')");
			while (res.next()) {
				String xpath = res.getString(1);
				resArray.add(xpath);
			}
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving dataviews subscribed to user : " + username
					+ " \nError is : " + e.toString(), "Critical");
			throw new RuntimeException(e);
		} finally {
			close();
			return resArray;
		}

	}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

public static void logoutDevice(String android_id)
{
	SQLConnect();

	try {
		stmt = conn.createStatement();
		res = stmt
				.executeQuery("call sp_logoutdevice('" + android_id + "')");
		logA.doLog("SQL" , "[SQL]Device " + android_id + " logged out successfully", "Info");
	} catch (SQLException e) {
		logA.doLog("SQL" , "[SQL]Error while logging out " + android_id + " \nError is : " + e.toString(), "Critical");
		throw new RuntimeException(e);
	} finally {
		close();
	}
}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

public static void loginDevice(String android_id)
{
	SQLConnect();

	try {
		stmt = conn.createStatement();
		res = stmt
				.executeQuery("call sp_logindevice('" + android_id + "')");
		logA.doLog("SQL" , "[SQL]Device " + android_id + " logged in successfully", "Info");
	} catch (SQLException e) {
		logA.doLog("SQL" , "[SQL]Error while logging in device " + android_id + " \nError is : " + e.toString(), "Critical");
		throw new RuntimeException(e);
	} finally {
		close();
	}
}
  
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

@SuppressWarnings("finally")
public static Map<String, HashMap<String, String>> getLiveDevices() {
	SQLConnect();
	Map<String, HashMap<String, String>> resMap = new HashMap<String, HashMap<String, String>>();
	try {
		stmt = conn.createStatement();
		res = stmt.executeQuery("call sp_get_live_devices()");
		// MailRoom.sendMail(username);
		while (res.next()) {
			String user = res.getString(1);
			String devID = res.getString(2);
			String key = res.getString(3);
				if(!resMap.containsKey(user))
					resMap.put(user, new HashMap<String, String>());
					resMap.get(user).put(devID, key);
		}
	} catch (SQLException e) {
		logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(), "Critical");
		// e.printStackTrace();
		throw new RuntimeException(e);
	} finally {
		close();
		return resMap;
	}

}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

public static HashMap<String,ArrayList<String>> getLivePaths(String query) {
	SQLConnect();
	HashMap<String,ArrayList<String>> resMap = new HashMap<String,ArrayList<String>>();
	try {
		stmt = conn.createStatement();
		res = stmt.executeQuery("select distinct x.xpath, u.username from user_paths as x join users as u on x.userid = u.userid where x.userid in (select userid from users where username in ( " + query + "));");
		// MailRoom.sendMail(username);
		while (res.next()) {
			String xpath = res.getString(1);
			String user = res.getString(2);
			if(!resMap.containsKey(xpath))
				resMap.put(xpath, new ArrayList<String>());
				resMap.get(xpath).add(user);
		}
	} catch (SQLException e) {
		logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(), "Critical");
		// e.printStackTrace();
		throw new RuntimeException(e);
	} finally {
		close();
		return resMap;
	}

}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

public static void execCustom(String query) {
	SQLConnect();
	try {
		stmt = conn.createStatement();
		stmt.executeUpdate(query);
		// MailRoom.sendMail(username);
	} catch (SQLException e) {
		logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(), "Critical");
		e.printStackTrace();
		throw new RuntimeException(e);
	} finally {
		close();
	}

}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

  // You need to close the res
  public static void close() {
    try {
      if (res != null) {
        res.close();
      }

      if (stmt != null) {
        stmt.close();
      }

      if (conn != null) {
        conn.close();
      }
    } catch (Exception e) {
    	logA.doLog("SQL" , "[SQL]SQL connection has failed to close! \nError is : " + e.toString(), "Critical");

    }
  }
  
  
  
  public static void setAddress(String submittedAddress)
  {
	  address = submittedAddress;
  }
  
  

} 
