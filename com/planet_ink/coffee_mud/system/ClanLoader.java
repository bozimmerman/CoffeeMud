package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

/**
  * ClanLoader handles Clan data
  * @author=Jeremy Vyska
  */
public class ClanLoader
{

  private static int currentRecordPos=1;
  private static int recordCount=0;

	public static void updateBootStatus(Host myHost, String loading)
	{
    myHost.setGameStatusStr("Booting: Loading "+loading+" ("+currentRecordPos+" of "+recordCount+")");
	}

  public static void DBRead(Host myHost)
	{
		DBConnection D=null;
    Clan C=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCLAN");
      R.last();
      recordCount=R.getRow();
      R.beforeFirst();
			while(R.next())
			{
        currentRecordPos=R.getRow();
        String name=DBConnections.getRes(R,"CMCLID");
        C=Clans.getClanType(Util.s_int(DBConnections.getRes(R,"CMTYPE")));
        C.setName(name);
        C.setPremise(DBConnections.getRes(R,"CMDESC"));
        C.setAcceptanceSettings(DBConnections.getRes(R,"CMACPT"));
        C.setPolitics(DBConnections.getRes(R,"CMPOLI"));
        C.setRecall(DBConnections.getRes(R,"CMRCLL"));
        C.setDonation(DBConnections.getRes(R,"CMDNAT"));
				Clans.addElement(C);
        updateBootStatus(myHost,"Clans");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Clan",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
    // log comment 
  }

  public static void DBUpdate(Clan C)
	{
		DBConnection D=null;
		String str=null;
    String sql=null;
		try
		{
			D=DBConnector.DBFetch();
			str="UPDATE CMCLAN SET "
				+"CMDESC='"+C.getPremise()+"',"
				+"CMACPT='"+C.getAcceptanceSettings()+"',"
				+"CMPOLI='"+C.getPolitics()+"',"
				+"CMRCLL='"+C.getRecall()+"',"
				+"CMDNAT='"+C.getDonation()+"'"
				+" WHERE CMCLID='"+C.ID()+"'";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Clan",str);
			Log.errOut("Clan",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
  }

  public static void DBCreate(Clan C)
	{
		if(C.ID().length()==0) return;
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			str="INSERT INTO CMCLAN ("
			+"CMCLID,"
      +"CMTYPE,"
			+"CMDESC,"
			+"CMACPT,"
			+"CMPOLI,"
			+"CMRCLL,"
			+"CMDNAT"
			+") values ("
			+"'"+C.ID()+"',"
      +""+C.getType()+","
			+"'"+C.getPremise()+"',"
      +"'"+C.getAcceptanceSettings()+"',"
      +"'"+C.getPolitics()+"',"
			+"'"+C.getRecall()+"',"
			+"'"+C.getDonation()+"')";
			D.update(str);
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Clan",str);
			Log.errOut("Clan",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
  }

  public static void DBDelete(Clan C)
	{
    DBConnection D=null;
    try
    {
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMCLAN WHERE CMCLID='"+C.ID()+"'");
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Clan","Delete"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
  }

}