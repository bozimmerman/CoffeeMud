package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
public class QuestLoader
{
	private static int recordCount=1;
	private static int currentRecordPos=1;
	private static int updateBreak=1;
	private final static String zeroes="000000000000";

	public static void DBRead(Host myHost)
	{
		Quests.shutdown();
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMQUESTS");
			R.last(); recordCount=R.getRow(); R.beforeFirst();
			updateBreak=1;
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String questScript=DBConnections.getRes(R,"CMQSCRPT");
				Quests Q=new Quests();
				Q.setScript(questScript);
				if((Q.name().length()>0)&&(Q.duration()>=0))
					Quests.addQuest(Q);
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("Quest",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
	}
	public static void DBUpdateQuests(Vector quests)
	{
		if(quests==null) quests=new Vector();
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();
			D.update("DELETE FROM CMQUESTS WHERE CMQUTYPE='Quests'");
			DBConnector.DBDone(D);
			for(int m=0;m<quests.size();m++)
			{
				Quest Q=(Quest)quests.elementAt(m);
				D=DBConnector.DBFetch();
				str=
				 "INSERT INTO CMQUESTS ("
				 +"CMQUESID, "
				 +"CMQUTYPE, "
				 +"CMQSCRPT "
				 +") values ("
				 +"'"+(""+Q)+"',"
				 +"'"+CMClass.className(Q)+"',"
				 +"'"+Q.script()+" '"
				 +")";
				D.update(str);
				DBConnector.DBDone(D);
			}
		}
		catch(SQLException sqle)
		{
			Log.errOut("Quest",str);
			Log.errOut("Quest","DBUpdateQuests"+sqle);
			if(D!=null) DBConnector.DBDone(D);
		}
	}
	
}
