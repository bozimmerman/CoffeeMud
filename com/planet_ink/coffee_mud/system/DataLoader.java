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
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment 
		return rows;
	}
	public static Vector DBReadClasses()
	{
		DBConnection D=null;
		Vector rows=new Vector();
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCCAC");
			while(R.next())
			{
				Vector V=new Vector();
				V.addElement(DBConnections.getRes(R,"CMCCID"));
				V.addElement(DBConnections.getRes(R,"CMCDAT"));
				rows.addElement(V);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
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
			ResultSet R=null;
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
			else
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'");
			while(R.next())
			{
				String playerID2=DBConnections.getRes(R,"CMPLID");
				String section2=DBConnections.getRes(R,"CMSECT");
				if(section2.equalsIgnoreCase(section))
				{
					Vector V=new Vector();
					V.addElement(playerID2);
					V.addElement(section2);
					V.addElement(DBConnections.getRes(R,"CMPKEY"));
					V.addElement(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(V);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment 
		return rows;
	}
	
	public static int DBCount(String playerID, String section)
	{
		DBConnection D=null;
		int rows=0;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=null;
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
			else
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'");
			while(R.next())
			{
				String playerID2=DBConnections.getRes(R,"CMPLID");
				String section2=DBConnections.getRes(R,"CMSECT");
				if(section2.equalsIgnoreCase(section))
					rows++;
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
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
			ResultSet R=null;
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				R=D.query("SELECT * FROM CMPDAT WHERE CMPKEY='"+key+"'");
			else
				R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'");
			while(R.next())
			{
				String playerID2=DBConnections.getRes(R,"CMPLID");
				String section2=DBConnections.getRes(R,"CMSECT");
				if((playerID2.equalsIgnoreCase(playerID))
				&&(section2.equalsIgnoreCase(section)))
				{
					Vector V=new Vector();
					V.addElement(playerID2);
					V.addElement(section2);
					V.addElement(DBConnections.getRes(R,"CMPKEY"));
					V.addElement(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(V);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
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
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
		// log comment 
		return rows;
	}

	public static void DBDelete(String playerID, String section)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
			{
				Vector keys=new Vector();
				ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
				while(R.next())
				{
					String section2=DBConnections.getRes(R,"CMSECT");
					if(section.equalsIgnoreCase(section2))
						keys.addElement(DBConnections.getRes(R,"CMPKEY"));
				}
				for(int i=0;i<keys.size();i++)
				{
					DBConnector.DBDone(D);
					D=DBConnector.DBFetch();
					D.update("DELETE FROM CMPDAT WHERE CMPKEY='"+((String)keys.elementAt(i))+"'",0);
				}
			}
			else
				D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'",0);
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}
	public static void DBDelete(String playerID, String section, String key)
	{
		
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			if((D.catalog()!=null)&&(D.catalog().equals("FAKEDB")))
				D.update("DELETE FROM CMPDAT WHERE CMPKEY='"+key+"'",0);
			else
				D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'",0);
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DBConnector.DBDone(D);
	}
	public static void DBDeleteRace(String raceID)
	{
		DBConnector.update("DELETE FROM CMGRAC WHERE CMRCID='"+raceID+"'");
	}
	public static void DBDeleteClass(String classID)
	{
		DBConnector.update("DELETE FROM CMCCAC WHERE CMCCID='"+classID+"'");
	}
	public static void DBDelete(String section)
	{
		DBConnector.update("DELETE FROM CMPDAT WHERE CMSECT='"+section+"'");
	}
	public static void DBCreateRace(String raceID, String data)
	{
		DBConnector.update(
		 "INSERT INTO CMGRAC ("
		 +"CMRCID, "
		 +"CMRDAT "
		 +") values ("
		 +"'"+raceID+"',"
		 +"'"+data+" '"
		 +")");
	}
	public static void DBCreateClass(String classID, String data)
	{
		DBConnector.update(
		 "INSERT INTO CMCCAC ("
		 +"CMCCID, "
		 +"CMCDAT "
		 +") values ("
		 +"'"+classID+"',"
		 +"'"+data+" '"
		 +")");
	}
	public static void DBCreate(String player, String section, String key, String data)
	{
		DBConnector.update(
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
		 +")");
	}
}
