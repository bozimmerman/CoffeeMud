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
		}
		return journal;
	}
	
	public static synchronized void DBWrite(String Journal, String from, String to, String subject, String message, int which)
	{
		String date=null;
		if(which>=0)
		{
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			if(which>=journal.size()) return;
			Vector entry=(Vector)journal.elementAt(which);
			String oldkey=(String)entry.elementAt(0);
			from=(String)entry.elementAt(1);
			date=(String)entry.elementAt(2);
			to=(String)entry.elementAt(3);
			subject=(String)entry.elementAt(4);
			message=((String)entry.elementAt(5))+"%0D---------------------------------------------%0DReply from: "+from+"%0D"+message;
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				String str="DELETE FROM CMJRNL WHERE CMJRNL='"+Journal+"' AND CMJKEY='"+oldkey+"'";
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
			date=new IQCalendar().getTime().getTime()+"";
		DBConnection D=null;
		String str=null;
		try
		{
			D=DBConnector.DBFetch();

			str="INSERT INTO CMJRNL ("
			+"CMJRNL, "
			+"CMJKEY, "
			+"CMFROM, "
			+"CMDATE, "
			+"CMTONM, "
			+"CMSUBJ, "
			+"CMMSGT "
			+") VALUES ('"
			+Journal
			+"','"+from+date
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
