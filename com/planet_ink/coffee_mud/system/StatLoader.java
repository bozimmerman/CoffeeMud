package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class StatLoader
{
	public static CoffeeTables DBRead(long startTime)
	{
		DBConnection D=null;
		CoffeeTables T=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMSTAT WHERE CMSTRT='"+startTime+"'");
			T=new CoffeeTables();
			if(R.next())
			{
				long endTime=DBConnections.getLongRes(R,"CMENDT");
				String data=DBConnections.getRes(R,"CMDATA");
				T.populate(startTime,endTime,data);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment 
		return T;
	}
	public static void DBDelete(long startTime)
	{
		try
		{
			DBConnector.update("DELETE FROM CMSTAT WHERE CMSTRT='"+startTime+"'");
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
	}
	public static void DBUpdate(long startTime, String data)
	{
		try
		{
			DBConnector.update("UPDATE CMSTAT SET CMDATA='"+data+"' WHERE CMSTRT='"+startTime+"'");
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
	}
	public static void DBCreate(long startTime, long endTime, String data)
	{
		DBConnector.update(
		 "INSERT INTO CMSTAT ("
		 +"CMSTRT, "
		 +"CMDATA "
		 +") values ("
		 +"'"+startTime+"',"
		 +"'"+data+"'"
		 +")");
	}
}