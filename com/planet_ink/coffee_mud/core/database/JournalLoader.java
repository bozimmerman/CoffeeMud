package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.sql.*;
import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class JournalLoader
{
	protected DBConnector DB=null;
	public JournalLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	
	public int DBCount(String Journal, String from, String to)
	{
		if(Journal==null) return 0;
		synchronized(Journal.toUpperCase().intern())
		{
			int ct=0;
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				ResultSet R=D.query("SELECT CMFROM,CMTONM FROM CMJRNL WHERE CMJRNL='"+Journal+"'");
				while(R.next())
				{
					if((from!=null)&&(!from.equalsIgnoreCase(DBConnections.getRes(R,"CMFROM"))))
					   continue;
					if((to!=null)&&(!to.equalsIgnoreCase(DBConnections.getRes(R,"CMTONM"))))
					   continue;
					ct++;
				}
				DB.DBDone(D);
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DB.DBDone(D);
				return ct;
			}
			return ct;
		}
	}
    
	
	
    public String DBGetRealName(String possibleName)
    {
        DBConnection D=null;
        String realName=null;
        try
        {
            D=DB.DBFetch(); // add unique
            ResultSet R=D.query("SELECT CMJRNL FROM CMJRNL WHERE CMJRNL='"+possibleName+"'");
            if(R.next())
            {
                realName=DBConnections.getRes(R,"CMJRNL");
                if(realName.length()==0)
                {
                    DB.DBDone(D);
                    return realName=null;
                }
            }
            DB.DBDone(D);
        }
        catch(Exception sqle)
        {
            Log.errOut("Journal",sqle);
            if(D!=null) DB.DBDone(D);
        }
        return realName;
    }
	
	public long[] DBJournalLatestDateNewerThan(String Journal, String to, long olderTime)
	{
		DBConnection D=null;
		long[] newest=new long[]{0,0};
		try
		{
			D=DB.DBFetch(); // add max and count to fakedb
			String str="SELECT CMUPTM FROM CMJRNL WHERE CMJRNL='"+Journal+"' AND CMUPTM > " + olderTime;
			if(to != null) str +=" AND CMTONM='"+to+"'";
			ResultSet R=D.query(str);
			while(R.next())
			{
				newest[1]++;
				long date=R.getLong("CMUPTM");
				if(date>newest[0])
					newest[0]=date;
			}
			DB.DBDone(D);
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			if(D!=null) DB.DBDone(D);
		}
		return newest;
	}
	
	public synchronized Vector<String> DBReadJournals()
	{
		DBConnection D=null;
		HashSet journalsH = new HashSet();
		Vector journals=new Vector();
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT CMJRNL FROM CMJRNL"); // add unique to fakedb
			while(R.next())
			{
				String which=DBConnections.getRes(R,"CMJRNL");
				if(!journalsH.contains(which)) {
					journalsH.add(which);
				    if((which.toUpperCase().startsWith("SYSTEM_"))
				    &&(journalsH.size()>0))
				    	journals.insertElementAt(which,0);
				    else
				    	journals.addElement(which);
				}
			}
			DB.DBDone(D);
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			if(D!=null) DB.DBDone(D);
			return null;
		}
		return journals;
	}
	
	protected JournalsLibrary.JournalEntry DBReadJournalEntry(ResultSet R)
	{
		JournalsLibrary.JournalEntry entry=new JournalsLibrary.JournalEntry();
		entry.key=DBConnections.getRes(R,"CMJKEY");
		entry.from=DBConnections.getRes(R,"CMFROM");
		String dateStr = DBConnections.getRes(R,"CMDATE");
		entry.to=DBConnections.getRes(R,"CMTONM");
		entry.subj=DBConnections.getRes(R,"CMSUBJ");
		entry.parent=DBConnections.getRes(R,"CMPART");
		entry.attributes=CMath.s_long(DBConnections.getRes(R,"CMATTR"));
		entry.data=DBConnections.getRes(R,"CMDATA");
		String uptm=DBConnections.getRes(R,"CMUPTM");
		entry.msg=DBConnections.getRes(R,"CMMSGT");
		
		int datestrdex=dateStr.indexOf("/");
		if(datestrdex>=0)
		{
			entry.update=CMath.s_long(dateStr.substring(datestrdex+1));
			entry.date=CMath.s_long(dateStr.substring(0,datestrdex));
		}
		else
		{
			entry.date=CMath.s_long(dateStr);
			long realUpdate=CMath.s_long(uptm);
			if(realUpdate > entry.date)
				entry.update=realUpdate;
			else
				entry.update=entry.date;
		}
		
		String subject=entry.subj.toUpperCase();
		if((subject.startsWith("MOTD"))
		||(subject.startsWith("MOTM"))
		||(subject.startsWith("MOTY")))
		{
			char c=subject.charAt(3);
			entry.subj=entry.subj.substring(4);
			long last=entry.date;
			if(c=='D') last=last+TimeManager.MILI_DAY;
			else
			if(c=='M') last=last+TimeManager.MILI_MONTH;
			else
			if(c=='Y') last=last+TimeManager.MILI_YEAR;
			entry.update=last;
		}
		return entry;
	}
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgsOlderThan(String Journal, String to, long newerDate)
	{
		Vector<JournalsLibrary.JournalEntry> journal=new Vector<JournalsLibrary.JournalEntry>();
		//Resources.submitResource("JOURNAL_"+Journal);
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String str="SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"' AND CMUPTM < " + newerDate;
			if(to != null) str +=" AND CMTONM='"+to+"'";
			ResultSet R=D.query(str);
			while(R.next())
			{
				JournalsLibrary.JournalEntry entry = DBReadJournalEntry(R); 
				journal.addElement(entry);
			}
			DB.DBDone(D);
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			if(D!=null) DB.DBDone(D);
			return null;
		}
		Collections.sort(journal);
		return journal;
	}
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgsNewerThan(String Journal, String to, long olderDate)
	{
		Vector<JournalsLibrary.JournalEntry> journal=new Vector<JournalsLibrary.JournalEntry>();
		//Resources.submitResource("JOURNAL_"+Journal);
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String str="SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"' AND CMUPTM > " + olderDate;
			if(to != null) str +=" AND CMTONM='"+to+"'";
			ResultSet R=D.query(str);
			while(R.next())
			{
				JournalsLibrary.JournalEntry entry = DBReadJournalEntry(R); 
				journal.addElement(entry);
			}
			DB.DBDone(D);
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			if(D!=null) DB.DBDone(D);
			return null;
		}
		Collections.sort(journal);
		return journal;
	}
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgs(String Journal)
	{
		if(Journal==null) return new Vector<JournalsLibrary.JournalEntry>();
		synchronized(Journal.toUpperCase().intern())
		{
			Vector<JournalsLibrary.JournalEntry> journal=new Vector<JournalsLibrary.JournalEntry>();
			//Resources.submitResource("JOURNAL_"+Journal);
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String str="SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"'";
				ResultSet R=D.query(str);
				while(R.next())
				{
					JournalsLibrary.JournalEntry entry = DBReadJournalEntry(R); 
					journal.addElement(entry);
				}
				DB.DBDone(D);
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DB.DBDone(D);
				return null;
			}
			Collections.sort(journal);
			return journal;
		}
	}

	public JournalsLibrary.JournalEntry DBReadJournalEntry(String Journal, String Key)
	{
		if(Journal==null) return null;
		synchronized(Journal.toUpperCase().intern())
		{
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				String str="SELECT * FROM CMJRNL WHERE CMJKEY='"+Key+"' AND CMJRNL='"+Journal+"'";
				ResultSet R=D.query(str);
				if(R.next())
				{
					JournalsLibrary.JournalEntry entry = DBReadJournalEntry(R);
					return entry;
				}
				DB.DBDone(D);
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DB.DBDone(D);
				return null;
			}
			return null;
		}
	}

	public int getFirstMsgIndex(Vector journal, String from, String to, String subj)
	{
		if(journal==null) return -1;
		for(int i=0;i<journal.size();i++)
		{
			JournalsLibrary.JournalEntry E=(JournalsLibrary.JournalEntry)journal.elementAt(i);
			if((from!=null)&&(!(E.from).equalsIgnoreCase(from)))
				continue;
			if((to!=null)&&(!(E.to).equalsIgnoreCase(to)))
				continue;
			if((subj!=null)&&(!(E.subj).equalsIgnoreCase(subj)))
				continue;
			return i;
		}
		return -1;
	}
	
	public void DBDelete(String oldkey)
	{
		DB.update("DELETE FROM CMJRNL WHERE CMJKEY='"+oldkey+"'");
	}
	
	public void DBUpdateJournal(String key, String subject, String msg)
	{
		DB.update("UPDATE CMJRNL SET CMSUBJ='"+subject+"', CMMSGT='"+msg+"' WHERE CMJKEY='"+key+"'");
	}
	
	public void DBDeletePlayerData(String name)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			D.update("DELETE FROM CMJRNL WHERE CMTONM='"+name+"'",0);
		}
		catch(Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		if(D!=null) DB.DBDone(D);
		
	}
	
	public void DBReadJournalSummaryStats(JournalsLibrary.JournalSummaryStats stats)
	{
		DBConnection D=null;
		String topKey = null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+stats.name+"' AND CMTONM='ALL'");
			long topTime = 0;
			while(R.next())
			{
				String key=R.getString("CMJKEY");
				String parentKey=R.getString("CMPART");
				long updateTime=R.getLong("CMUPTM");
				stats.posts++;
				if((parentKey!=null)&&(parentKey.length()>0))
					stats.threads++;
				else
				if(updateTime>topTime)
				{
					topTime=updateTime;
					topKey=key;
				}
			}
			R.close();
		}
		catch(Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		if(topKey != null)
			stats.latest = DBReadJournalEntry(stats.name, topKey);
		else
			stats.latest = null;
		stats.imagePath="";
		stats.shortIntro="[This is the short journal description.  To change it, create a journal entry addressed to JOURNALINTRO.]";
		stats.longIntro="[This is the long journal description.  To change it, create a journal entry addressed to JOURNALINTRO.]";
		try
		{
			if(D==null) 
				D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+stats.name+"' AND CMTONM='JOURNALINTRO'");
			if(R.next())
			{
				JournalsLibrary.JournalEntry entry = this.DBReadJournalEntry(R);
				if(entry != null)
				{
					stats.introKey=entry.key;
					stats.longIntro=entry.msg;
					stats.shortIntro=entry.subj;
					stats.imagePath=entry.data;
				}
			}
			R.close();
		}
		catch(Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		if(D!=null) DB.DBDone(D);
	}

	
	public void DBDelete(String Journal, String key)
	{
		if(Journal==null) return;
		synchronized(Journal.toUpperCase().intern())
		{
			if(key!=null)
			{
				DB.update("DELETE FROM CMJRNL WHERE CMJKEY='"+key+"'");
			}
			else
			{
				DB.update("DELETE FROM CMJRNL WHERE CMJRNL='"+Journal+"'");
			}
		}
	}
	
	public void DBWriteJournalReply(String Journal,
									String key,
									String from, 
									String to, 
									String subject, 
									String message)
	{
		if(Journal==null) return;
		synchronized(Journal.toUpperCase().intern())
		{
			JournalsLibrary.JournalEntry entry=DBReadJournalEntry(Journal, key);
			if(entry==null) return;
			long now=System.currentTimeMillis();
			String oldkey=entry.key;
			String oldmsg=entry.msg;
			message=oldmsg+JournalsLibrary.JOURNAL_BOUNDARY
			 +"^yReply from^N: "+from+"%0D"
			 +"^yDate/Time ^N: "+CMLib.time().date2String(now)+"%0D"
			 +message;
			DB.update("UPDATE CMJRNL SET CMUPTM="+now+", CMMSGT='"+message+"' WHERE CMJKEY='"+oldkey+"'");
		}
	}
	
	public void DBWrite(String Journal,
						String from, 
						String to, 
						String subject,
						String message)
	{
		JournalsLibrary.JournalEntry entry = new JournalsLibrary.JournalEntry();
		entry.key=null;
		entry.from=from;
		entry.date=System.currentTimeMillis();
		entry.to=to;
		entry.subj=subject;
		entry.msg=message;
		entry.update=System.currentTimeMillis();
		DBWrite(Journal, entry);
	}
	
	public void DBWrite(String Journal, JournalsLibrary.JournalEntry entry)
	{
		if(Journal==null) return;
		synchronized(Journal.toUpperCase().intern())
		{
			long now=System.currentTimeMillis();
			if(entry==null) return;
			if(entry.subj.length()>255) 
				entry.subj=entry.subj.substring(0,255);
			if(entry.key==null)
				entry.key=(Journal+now+Math.random());
			if(entry.date==0)
				entry.date=now;
			if(entry.update==0)
				entry.update=now;
			DB.update(
			"INSERT INTO CMJRNL ("
			+"CMJKEY, "
			+"CMJRNL, "
			+"CMFROM, "
			+"CMDATE, "
			+"CMTONM, "
			+"CMSUBJ, "
			+"CMPART, "
			+"CMATTR, "
			+"CMDATA, "
			+"CMUPTM, "
			+"CMMSGT "
			+") VALUES ('"
			+entry.key
			+"','"+Journal
			+"','"+entry.from
			+"','"+entry.date
			+"','"+entry.to
			+"','"+entry.subj
			+"','"+entry.parent
			+"',"+entry.attributes
			+",'"+entry.data
			+"',"+entry.update
			+",'"+entry.msg+"')");
			if(System.currentTimeMillis()==now) // ensures unique keys.
				try{Thread.sleep(1);}catch(Exception e){}
		}
	}
}
