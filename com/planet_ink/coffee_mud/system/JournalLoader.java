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
		Vector journal=new Vector();
		if(Journal==null)
		{
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				ResultSet R=D.query("SELECT * FROM CMJRNL");
				while(R.next())
				{
					String which=DBConnections.getRes(R,"CMJRNL");
					if(!journal.contains(which))
						journal.addElement(which);
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
		else
		{
			//Resources.submitResource("JOURNAL_"+Journal);
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				String str="SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"'";
				ResultSet R=D.query(str);
				while(R.next())
				{
					Vector entry=new Vector();
					entry.addElement(DBConnections.getRes(R,"CMJKEY"));
					entry.addElement(DBConnections.getRes(R,"CMFROM"));
					String datestr=DBConnections.getRes(R,"CMDATE");
					entry.addElement(datestr);
					entry.addElement(DBConnections.getRes(R,"CMTONM"));
					entry.addElement(DBConnections.getRes(R,"CMSUBJ"));
					entry.addElement(DBConnections.getRes(R,"CMMSGT"));
					
					int datestrdex=datestr.indexOf("/");
					if(datestrdex>=0)
					{
						entry.addElement(datestr.substring(datestrdex+1));
						entry.setElementAt(datestr.substring(0,datestrdex),2);
					}
					else
						entry.addElement(datestr);
					
					String subject=(String)entry.elementAt(4);
					if((subject.startsWith("MOTD"))
					||(subject.startsWith("MOTM"))
					||(subject.startsWith("MOTY")))
					{
						char c=subject.charAt(3);
						subject=subject.substring(4);
						entry.setElementAt(subject,4);
						long last=Util.s_long((String)entry.elementAt(2));
						if(c=='D') last=last+((long)(1000*60*60*24));
						else
						if(c=='M') last=last+((long)(1000*60*60*24*30));
						else
						if(c=='Y') last=last+((long)(1000*60*60*24*365));
						entry.setElementAt(""+last,6);
					}
					
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
				
			Vector oldJournal=journal;
			journal=new Vector();
			while(oldJournal.size()>0)
			{
				Vector useEntry=null;
				long byDate=Long.MAX_VALUE;
				for(int j=0;j<oldJournal.size();j++)
				{
					Vector entry=(Vector)oldJournal.elementAt(j);
					String datestr=(String)entry.elementAt(2);
					long date=0;
					if(datestr.indexOf("/")>=0)
						date=Util.s_long(datestr.substring(datestr.indexOf("/")+1));
					else
						date=Util.s_long(datestr);
					
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
		}
		return journal;
	}
	public static synchronized Vector DBReadCached(String Journal)
	{
		if(Journal==null) return DBRead(Journal);
		Vector journal=(Vector)Resources.getResource("JOURNAL_"+Journal);
		if(journal==null)
		{
			journal=DBRead(Journal);
		}
		if(journal!=null)
			Resources.submitResource("JOURNAL_"+Journal,journal);
		return journal;
	}

	public static int getFirstMsgIndex(Vector journal, 
									   String from, 
									   String to, 
									   String subj)
	{
		if(journal==null) return -1;
		for(int i=0;i<journal.size();i++)
		{
			Vector V=(Vector)journal.elementAt(i);
			if((from!=null)&&(!((String)V.elementAt(1)).equalsIgnoreCase(from)))
				continue;
			if((to!=null)&&(!((String)V.elementAt(3)).equalsIgnoreCase(to)))
				continue;
			if((subj!=null)&&(!((String)V.elementAt(4)).equalsIgnoreCase(subj)))
				continue;
			return i;
		}
		return -1;
	}
	
	public static synchronized void DBDelete(String Journal, int which)
	{
		DBConnection D=null;
		if(which<0)
		{
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			try
			{
				D=DBConnector.DBFetch();
				String str="DELETE FROM CMJRNL WHERE CMJRNL='"+Journal+"'";
				D.update(str,0);
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
		if(which==Integer.MAX_VALUE)
		{
			try
			{
				D=DBConnector.DBFetch();
				String str="DELETE FROM CMJRNL WHERE CMJKEY='"+Journal+"'";
				D.update(str,0);
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
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			if(which>=journal.size()) return;
			Vector entry=(Vector)journal.elementAt(which);
			String oldkey=(String)entry.elementAt(0);
			try
			{
				D=DBConnector.DBFetch();
				String str="DELETE FROM CMJRNL WHERE CMJKEY='"+oldkey+"'";
				D.update(str,0);
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
	
	
	public static synchronized void DBWrite(String Journal, 
											String from, 
											String to, 
											String subject, 
											String message, 
											int which)
	{
		String date=System.currentTimeMillis()+"";
		if(which>=0)
		{
			Vector journal=DBRead(Journal);
			if(journal==null) return;
			if(which>=journal.size()) return;
			Vector entry=(Vector)journal.elementAt(which);
			String olddate=(String)entry.elementAt(2);
			int olddatedex=olddate.indexOf("/");
			if(olddatedex>=0) olddate=olddate.substring(0,olddatedex);
			String oldkey=(String)entry.elementAt(0);
			String oldmsg=(String)entry.elementAt(5);
			message=oldmsg+"%0D---------------------------------------------%0DReply from: "+from+"%0D"+message;
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				String str="UPDATE CMJRNL SET CMDATE='"+olddate+"/"+date+"', CMMSGT='"+message+"' WHERE CMJKEY='"+oldkey+"'";
				D.update(str,0);
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
				+(Journal+from+date+Math.random())
				+"','"+Journal
				+"','"+from
				+"','"+date
				+"','"+to
				+"','"+subject
				+"','"+message+"');";
				D.update(str,0);
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
