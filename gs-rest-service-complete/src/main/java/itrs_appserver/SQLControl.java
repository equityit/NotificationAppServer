package itrs_appserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

public class SQLControl {
	
  private static String address = null;
  private static Connection conn = null;
  private static Statement stmt = null;
  private static ResultSet res = null;
  private static ResultSet res1 = null;
  public static Random random = new Random(System.currentTimeMillis());

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
        System.out.println(e);
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////
  
@SuppressWarnings("finally")
public static int checkUser(String username, String android_id) {
		SQLConnect();
		int result = 0;
		int queryOut = 0;
		System.out.println(username + "    " + android_id);
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
			System.out.println(e);
			throw new RuntimeException(e);
		} finally {
			close();
			return result;
		}

	}



private static int validDeviceCheck(int result, int queryOut, String android_id) throws SQLException {	//CREATE STORED PROCEDURE
	stmt = conn.createStatement();
	res1 = stmt.executeQuery("select id from devices where userid = " + queryOut + " and active = 1 and android_id = '" + android_id + "'");
	if (!res1.isBeforeFirst()) 
	{
		System.out.println("User has yet to verify this device"); // Username exists but no valid devices
		return 1;
	} 
	else 
	{
		result = 2; // Username is present and they have a a validated device
	}
	return result;
}



private static int extractUserID(int queryOut) throws SQLException {
	while (res.next()) 
	{
		int value = res.getInt(1);
		queryOut = value;
		System.out.println("RETURNED USER ID : " + queryOut);
	}
	return queryOut;
}



public static int checkValidDomain(String username) throws SQLException {
	SQLConnect();
	int ret;
	int queryOut = 0;
	try{
	stmt = conn.createStatement();
	res = stmt.executeQuery("call sp_Confirm_Valid_Domain('" + username +"')"); // Create query that extracts the domain from username and checks it against db white list, if correct then go into creation method
	if (!res.isBeforeFirst()) 
	{
		System.out.println("Invalid domain"); // Username exists but the password is incorrect
		ret = 0;
	} 
	else
	{
		System.out.println("Valid domain");
		ret = 1;
	}
	close();
	return ret;
	}
	catch(Exception e)
	{
		e.printStackTrace();
		throw new RuntimeException(e);
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////

  public static void findUser() throws Exception {
      try{
      // Statements allow to issue SQL queries to the database
      stmt = conn.createStatement();
      // Result set get the result of the SQL query
      res = stmt
          .executeQuery("select * from users");
      if(!res.isBeforeFirst())
      {
    	  System.out.println("no entry");
      }
      writeResultSet(res);
      System.out.println(res);

      }
     catch (Exception e) {
      throw e;
    } finally {
      close();
    }
  }
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
  

  private static void writeResultSet(ResultSet res) throws SQLException {
    // ResultSet is initially before the first data set
    while (res.next()) {
      // It is possible to get the columns via name
      // also possible to get the columns via the column number
      // which starts at 1
      // e.g. res.getSTring(2);
      String username = res.getString(2);
      String usertype = res.getString(3);
      System.out.println("Username: " + username);
      System.out.println("usertype: " + usertype);
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
				MailRoom.sendMail(username, ran, android_id);
			} catch (SQLException e) {
				e.printStackTrace();
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
		MailRoom.sendMail(username, ran, android_id);
	} catch (SQLException e) {
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
			e.printStackTrace();
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
			// MailRoom.sendMail(username);
		} catch (SQLException e) {
			e.printStackTrace();
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
	//MailRoom.sendMail(username);
	} catch (SQLException e) {
	e.printStackTrace();
	throw new RuntimeException(e);
	}
	finally{
	close();	
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////

public static ArrayList<String> getUserDataviewList(String username)
{
SQLConnect();
ArrayList<String> resArray = new ArrayList<String>();
try {
stmt = conn.createStatement();
res = stmt.executeQuery("call sp_Get_User_Dataviews('" + username + "')");
//MailRoom.sendMail(username);
while (res.next()) 
{
	String xpath = res.getString(1);
	resArray.add(xpath);
}
} catch (SQLException e) {
e.printStackTrace();
throw new RuntimeException(e);
}
finally{
close();
return resArray;
}


}
  
  // You need to close the res
  private static void close() {
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

    }
  }
  
  
  
  public static void setAddress(String submittedAddress)
  {
	  address = submittedAddress;
  }
  
  
  

  
  

} 







/*@SuppressWarnings("finally")
public static int checkUser(String username, String password) {
		SQLConnect();
		int result = 0;
		int userid = 0;
		System.out.println(username + "    " + password);
		try {

			stmt = conn.createStatement();
			res = stmt.executeQuery("select userId from users where username like '" + username + "'");

			if (!res.isBeforeFirst()) {
				System.out.println("Incorrect username or password");
			}

			else {
				while (res.next()) {
					int value = res.getInt(1);
					userid = value;
					System.out.println("RETURNED USER ID : " + userid);
				}
				res1 = stmt.executeQuery("select passwordhash from password where userid =" + userid);

				if (!res1.isBeforeFirst()) {
					System.out.println("Incorrect username or password");
				} else {
					result = userid;
				}
			}

		} catch (Exception e) {
			System.out.println(e);
			throw e;
		} finally {
			close();
			return result;
		}

	}*/