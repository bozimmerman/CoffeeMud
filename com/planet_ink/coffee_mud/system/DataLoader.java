package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class DataLoader
{
	public static Vector DBReadRaces()
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMGRAC");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMRCID"));
				V.addElement(DBConnections.getRes(R,"CMRDAT"));
				rows.addElement(V);
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		// log comment 
		return rows;
	}
	public static Vector DBRead(String playerID, String section)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' and CMSECT='"+section+"'");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMPLID"));
				V.addElement(DBConnections.getRes(R,"CMSECT"));
				V.addElement(DBConnections.getRes(R,"CMPKEY"));
				V.addElement(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(V);
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		// log comment 
		return rows;
	}
	public static Vector DBRead(String playerID, String section, String key)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMPLID"));
				V.addElement(DBConnections.getRes(R,"CMSECT"));
				V.addElement(DBConnections.getRes(R,"CMPKEY"));
				V.addElement(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(V);
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		// log comment 
		return rows;
	}
	public static Vector DBRead(String section)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMPLID"));
				V.addElement(DBConnections.getRes(R,"CMSECT"));
				V.addElement(DBConnections.getRes(R,"CMPKEY"));
				V.addElement(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(V);
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
		// log comment 
		return rows;
	}

	public static void DBDelete(String playerID, String section)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"' and CMSECT='"+section+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
	public static void DBDelete(String playerID, String section, String key)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
	public static void DBDeleteRace(String raceID)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMGRAC WHERE CMRCID='"+raceID+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
	public static void DBDelete(String section)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMPDAT WHERE CMSECT='"+section+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
	public static void DBCreateRace(String raceID, String data)
	{
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str=
			 "INSERT INTO CMGRAC ("
			 +"CMRCID, "
			 +"CMRDAT "
			 +") values ("
			 +"'"+raceID+"',"
			 +"'"+data+" '"
			 +")";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",str);
			Log.errOut("DataLoader","DBCreateRace"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
	public static void DBCreate(String player, String section, String key, String data)
	{
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str=
			 "INSERT INTO CMPDAT ("
			 +"CMPLID, "
			 +"CMSECT, "
			 +"CMPKEY, "
			 +"CMPDAT "
			 +") values ("
			 +"'"+player+"',"
			 +"'"+section+"',"
			 +"'"+key+"',"
			 +"'"+data+" '"
			 +")";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",str);
			Log.errOut("DataLoader","DBCreate"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
}
