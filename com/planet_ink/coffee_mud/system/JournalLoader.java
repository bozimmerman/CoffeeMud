package com.planet_ink.coffee_mud.system;
import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class JournalLoader
{
	public static synchronized Vector DBRead(String Journal)
	{
		Vector journal=null;//(Vector)Resources.getResource("JOURNAL_"+Journal);
		if(journal==null)
		{
			journal=new Vector();
			//Resources.submitResource("JOURNAL_"+Journal);
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"'");
				while(R.next())
				{
					Vector entry=new Vector();
					entry.addElement(DBConnections.getRes(R,"CMJKEY"));
					entry.addElement(DBConnections.getRes(R,"CMFROM"));
					entry.addElement(DBConnections.getRes(R,"CMDATE"));
					entry.addElement(DBConnections.getRes(R,"CMTONM"));
					entry.addElement(DBConnections.getRes(R,"CMSUBJ"));
					entry.addElement(DBConnections.getRes(R,"CMMSGT"));
					journal.addElement(entry);
				}
				DBConnector.DBDone(D);
			}
			catch(SQLException sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DBConnector.DBDone(D);
				return null;
			}
			
			// sorting SUCKED  I like KNOWING where the messages will be
			/*
			Vector oldJournal=journal;
			journal=new Vector();
			while(oldJournal.size()>0)
			{
				Vector useEntry=null;
				long byDate=Long.MAX_VALUE;
				for(int j=0;j<oldJournal.size();j++)
				{
					Vector entry=(Vector)oldJournal.elementAt(j);
					long date=Util.s_long((String)entry.elementAt(2));
					if(date<byDate)
					{
						byDate=date;
						useEntry=entry;
					}
				}
				if(useEntry!=null)
				{
					oldJournal.removeElement(useEntry);
					journal.addElement(useEntry);
				}
			}
			*/
		}
		return journal;
	}
	
	public static synchronized void DBDelete(String Journal, int which)
	{
		Vector journal=DBRead(Journal);
		if(journal==null) return;
		DBConnection D=null;
		if(which<0)
		{
			try
			{
				D=DBConnector.DBFetch();
				String str="DELETE FROM CMJRNL WHERE CMJRNL='"+Journal+"'";
				D.update(str);
				DBConnector.DBDone(D);
			}
			catch(SQLException sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DBConnector.DBDone(D);
				return;
			}
		}
		else
		{
			if(which>=journal.size()) return;
			Vector entry=(Vector)journal.elementAt(which);
			String oldkey=(String)entry.elementAt(0);
			try
			{
				D=DBConnector.DBFetch();
				String str="DELETE FROM CMJRNL WHERE CMJKEY='"+oldkey+"'";
				D.update(str);
				DBConnector.DBDone(D);
			}
			catch(SQLException sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DBConnector.DBDone(D);
				return;
			}
		}
	}
	
	
	public static synchronized void DBWrite(String Journal, String from, String to, String subject, String message, int which)
	{
		String date=new IQCalendar().getTime().getTime()+"";
		if(which>=0)
		{
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			if(which>=journal.size()) return;
			Vector entry=(Vector)journal.elementAt(which);
			String oldkey=(String)entry.elementAt(0);
			String oldmsg=(String)entry.elementAt(5);
			message=oldmsg+"%0D---------------------------------------------%0DReply from: "+from+"%0D"+message;
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				String str="UPDATE CMJRNL SET CMDATE='"+date+"', CMMSGT='"+message+"' WHERE CMJKEY='"+oldkey+"'";
				D.update(str);
				DBConnector.DBDone(D);
			}
			catch(SQLException sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DBConnector.DBDone(D);
				return;
			}
		}
		else
		{
			DBConnection D=null;
			String str=null;
			try
			{
				D=DBConnector.DBFetch();

				str="INSERT INTO CMJRNL ("
				+"CMJKEY, "
				+"CMJRNL, "
				+"CMFROM, "
				+"CMDATE, "
				+"CMTONM, "
				+"CMSUBJ, "
				+"CMMSGT "
				+") VALUES ('"
				+Journal+from+date
				+"','"+Journal
				+"','"+from
				+"',"+date
				+",'"+to
				+"','"+subject
				+"','"+message+"');";
				D.update(str);
				DBConnector.DBDone(D);
			}
			catch(SQLException sqle)
			{
				Log.errOut("Journal",str);
				Log.errOut("Journal","Create:"+sqle);
				if(D!=null) DBConnector.DBDone(D);
			}
		}
	}
}
