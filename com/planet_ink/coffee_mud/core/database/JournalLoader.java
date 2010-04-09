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
		Journal = DB.injectionClean(Journal);
		from = DB.injectionClean(from);
		to = DB.injectionClean(to);
		
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
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				return ct;
			}
			finally
			{
				if(D!=null) DB.DBDone(D);
			}
			return ct;
		}
	}
    
	
	
    public String DBGetRealName(String possibleName)
    {
    	possibleName = DB.injectionClean(possibleName);
		
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
                    return realName=null;
                }
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("Journal",sqle);
        }
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
        return realName;
    }
	
	public long[] DBJournalLatestDateNewerThan(String Journal, String to, long olderTime)
	{
		Journal = DB.injectionClean(Journal);
		to = DB.injectionClean(to);
		
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
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
		}
		finally
		{
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
				if(!journalsH.contains(which)) 
				{
					journalsH.add(which);
			    	journals.addElement(which);
				}
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
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
		entry.update=CMath.s_long(DBConnections.getRes(R,"CMUPTM"));
		entry.msgIcon=DBConnections.getRes(R, "CMIMGP");
		entry.views=CMath.s_int(DBConnections.getRes(R, "CMVIEW"));
		entry.replies=CMath.s_int(DBConnections.getRes(R, "CMREPL"));
		entry.msg=DBConnections.getRes(R,"CMMSGT");
		
		int datestrdex=dateStr.indexOf('/');
		if(datestrdex>=0)
		{
			entry.update=CMath.s_long(dateStr.substring(datestrdex+1));
			entry.date=CMath.s_long(dateStr.substring(0,datestrdex));
		}
		else
		{
			entry.date=CMath.s_long(dateStr);
			if(entry.update<entry.date)
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
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalPageMsgs(String Journal, String parent, String searchStr, long newerDate, int limit)
	{
		Journal = DB.injectionClean(Journal);
		parent = DB.injectionClean(parent);
		searchStr = DB.injectionClean(searchStr);
		
		Vector<JournalsLibrary.JournalEntry> journal=new Vector<JournalsLibrary.JournalEntry>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String str="SELECT * FROM CMJRNL WHERE";
			if((parent==null)||(parent.length()>0)||(newerDate==0))
				str+=" CMUPTM > " + newerDate;
			else
				str+=" CMUPTM < " + newerDate;
			
			if((Journal!=null)&&(Journal.length()>0))
				str += " AND CMJRNL='"+Journal+"'";
			if(parent != null)
				str += " AND CMPART='"+parent+"'";
			if((searchStr!=null)&&(searchStr.length()>0))
				str += " AND (CMSUBJ LIKE '%"+searchStr+"%' OR CMMSGT LIKE '%"+searchStr+"%')";
			str += " ORDER BY CMUPTM";
			if((parent==null)||(parent.length()>0))
				str += " ASC";
			else
				str += " DESC";
			
			ResultSet R=D.query(str);
			int cardinal=0;
			JournalsLibrary.JournalEntry entry; 
			while((cardinal < limit) && R.next())
			{
				entry = DBReadJournalEntry(R); 
				if((parent!=null)&&(CMath.bset(entry.attributes,JournalsLibrary.JournalEntry.ATTRIBUTE_STUCKY)))
					continue;
				entry.cardinal = ++cardinal;
				journal.addElement(entry);
			}
			if((journal.size()>0)&&(parent!=null)) // set last entry -- make sure its not stucky
			{
				journal.lastElement().isLastEntry=true;
				while(R.next())
				{
					long attributes=R.getLong("CMATTR");
					if(CMath.bset(attributes,JournalsLibrary.JournalEntry.ATTRIBUTE_STUCKY))
						continue;
					journal.lastElement().isLastEntry=false;
					break;
				}
			}
			else
			if((journal.size()>0)&&(!R.next()))
				journal.lastElement().isLastEntry=true;
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
		return journal;
	}
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgsNewerThan(String Journal, String to, long olderDate)
	{
		Journal = DB.injectionClean(Journal);
		to = DB.injectionClean(to);
		
		Vector<JournalsLibrary.JournalEntry> journal=new Vector<JournalsLibrary.JournalEntry>();
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			String str="SELECT * FROM CMJRNL WHERE CMUPTM > " + olderDate;
			if(Journal!=null) str +=" AND CMJRNL='"+Journal+"'";
			if(to != null) str +=" AND CMTONM='"+to+"'";
			ResultSet R=D.query(str);
			int cardinal=0;
			while(R.next())
			{
				JournalsLibrary.JournalEntry entry = DBReadJournalEntry(R); 
				entry.cardinal = ++cardinal;
				journal.addElement(entry);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
			return null;
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
		Collections.sort(journal);
		return journal;
	}
	
	public Vector<JournalsLibrary.JournalEntry> DBReadJournalMsgs(String Journal)
	{
		Journal = DB.injectionClean(Journal);
		
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
				int cardinal=0;
				while(R.next())
				{
					JournalsLibrary.JournalEntry entry = DBReadJournalEntry(R);
					entry.cardinal = ++cardinal;
					journal.addElement(entry);
				}
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				return null;
			}
			finally
			{
				if(D!=null) DB.DBDone(D);
			}
			Collections.sort(journal);
			return journal;
		}
	}

	public JournalsLibrary.JournalEntry DBReadJournalEntry(String Journal, String Key)
	{
		Journal = DB.injectionClean(Journal);
		Key = DB.injectionClean(Key);
		
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
			}
			catch(Exception sqle)
			{
				Log.errOut("Journal",sqle);
				return null;
			}
			finally
			{
				if(D!=null) DB.DBDone(D);
			}
			return null;
		}
	}

	public int getFirstMsgIndex(Vector journal, String from, String to, String subj)
	{
		from = DB.injectionClean(from);
		to = DB.injectionClean(to);
		subj = DB.injectionClean(subj);
		
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
	
	public void DBUpdateJournal(String key, String subject, String msg, long newAttributes)
	{
		key = DB.injectionClean(key);
		subject = DB.injectionClean(subject);
		msg = DB.injectionClean(msg);
		
		DB.update("UPDATE CMJRNL SET CMSUBJ='"+subject+"', CMMSGT='"+msg+"', CMATTR="+newAttributes+" WHERE CMJKEY='"+key+"'");
	}
	
	public void DBUpdateJournal(String Journal, JournalsLibrary.JournalEntry entry)
	{
		Journal = DB.injectionClean(Journal);
		entry.data = DB.injectionClean(entry.data);
		entry.from = DB.injectionClean(entry.from);
		entry.key = DB.injectionClean(entry.key);
		entry.msg = DB.injectionClean(entry.msg);
		entry.msgIcon = DB.injectionClean(entry.msgIcon);
		entry.parent = DB.injectionClean(entry.parent);
		entry.subj = DB.injectionClean(entry.subj);
		entry.to = DB.injectionClean(entry.to);
		
		DB.update("UPDATE CMJRNL SET "
				 +"CMFROM='"+entry.from+"', "
				 +"CMDATE="+entry.date+" , "
				 +"CMTONM='"+entry.to+"' "
				 +"CMSUBJ='"+entry.subj+"' "
				 +"CMPART='"+entry.parent+"' "
				 +"CMATTR="+entry.attributes+" "
				 +"CMDATA='"+entry.data+"' "
				 +"WHERE CMJRNL='"+Journal+"' AND CMJKEY='"+entry.key+"'");
		DB.update("UPDATE CMJRNL SET "
				 +"CMUPTM="+entry.update+", "
				 +"CMIMGP='"+entry.msgIcon+"', "
				 +"CMVIEW="+entry.views+", "
				 +"CMREPL="+entry.replies+", "
				 +"CMMSGT='"+entry.msg+"' "
				 +"WHERE CMJRNL='"+Journal+"' AND CMJKEY='"+entry.key+"'");
	}
	
	public void DBTouchJournalMessage(String key)
	{
		key = DB.injectionClean(key);
		DB.update("UPDATE CMJRNL SET CMUPTM="+System.currentTimeMillis()+" WHERE CMJKEY='"+key+"'");
	}
	
	public void DBUpdateMessageReplies(String key, int numReplies)
	{
		key = DB.injectionClean(key);
		DB.update("UPDATE CMJRNL SET CMUPTM="+System.currentTimeMillis()+", CMREPL="+numReplies+" WHERE CMJKEY='"+key+"'");
	}
	
	public void DBViewJournalMessage(String key, int views)
	{
		key = DB.injectionClean(key);
		DB.update("UPDATE CMJRNL SET CMVIEW="+views+" WHERE CMJKEY='"+key+"'");
	}
	
	public void DBDeletePlayerData(String name)
	{
		name = DB.injectionClean(name);
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			Vector<String> deletableEntriesV=new Vector<String>();
			Vector<String[]> notifiableParentsV=new Vector<String[]>();
			//Resources.submitResource("JOURNAL_"+Journal);
			String str="SELECT * FROM CMJRNL WHERE CMTONM='"+name+"'";
			ResultSet R=D.query(str);
			while(R.next())
			{
				String journalName=R.getString("CMJRNL");
				if((CMLib.journals().getForumJournal(journalName)!=null)
				||(CMLib.journals().getCommandJournal(journalName)!=null))
					continue;
				String key=R.getString("CMJKEY");
				String parent=R.getString("CMPART");
				if((parent!=null)&&(parent.length()>0))
					notifiableParentsV.add(new String[]{journalName,parent});
				deletableEntriesV.add(key);
			}
			for(String[] parentKey : notifiableParentsV)
			{
				if(!deletableEntriesV.contains(parentKey[0]))
				{
					JournalsLibrary.JournalEntry parentEntry=DBReadJournalEntry(parentKey[0], parentKey[1]);
					if(parentEntry!=null)
						DBUpdateMessageReplies(parentEntry.key,parentEntry.replies-1);
				}
			}
			for(String s : deletableEntriesV)
				D.update("DELETE FROM CMJRNL WHERE CMJKEY='"+s+"' OR CMPART='"+s+"'",0);
		}
		catch(Exception sqle)
		{
			Log.errOut("Journal",sqle);
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
	}
	
	public void DBUpdateJournalStats(String Journal, JournalsLibrary.JournalSummaryStats stats)
	{
		Journal = DB.injectionClean(Journal);
		stats.imagePath = DB.injectionClean(stats.imagePath);
		stats.introKey = DB.injectionClean(stats.introKey);
		stats.longIntro = DB.injectionClean(stats.longIntro);
		stats.name = DB.injectionClean(stats.name);
		stats.shortIntro = DB.injectionClean(stats.shortIntro);
		
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+stats.name+"' AND CMTONM='JOURNALINTRO'");
			JournalsLibrary.JournalEntry entry = null;
			if(R.next())
				entry = this.DBReadJournalEntry(R);
			R.close();
			if(entry!=null)
			{
				entry.subj = stats.shortIntro;
				entry.msg = stats.longIntro;
				entry.data=stats.imagePath;
				entry.attributes = JournalsLibrary.JournalEntry.ATTRIBUTE_PROTECTED;
				entry.date=-1;
				entry.update=-1;
				DBUpdateJournal(Journal,entry);
			}
			else
			{
				entry = new JournalsLibrary.JournalEntry();
				entry.subj =stats.shortIntro;
				entry.msg =stats.longIntro;
				entry.data=stats.imagePath;
				entry.to="JOURNALINTRO";
				entry.from="";
				entry.attributes = JournalsLibrary.JournalEntry.ATTRIBUTE_PROTECTED;
				entry.date=-1;
				entry.update=-1;
				this.DBWrite(Journal, entry);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
	}
	
	public void DBReadJournalSummaryStats(JournalsLibrary.JournalSummaryStats stats)
	{
		stats.imagePath = DB.injectionClean(stats.imagePath);
		stats.introKey = DB.injectionClean(stats.introKey);
		stats.longIntro = DB.injectionClean(stats.longIntro);
		stats.name = DB.injectionClean(stats.name);
		stats.shortIntro = DB.injectionClean(stats.shortIntro);
		
		DBConnection D=null;
		String topKey = null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+stats.name+"' AND CMTONM='ALL' AND CMPART=''");
			long topTime = 0;
			while(R.next())
			{
				String key=R.getString("CMJKEY");
				long updateTime=R.getLong("CMUPTM");
				long attributes=R.getLong("CMATTR");
				int replies=R.getInt("CMREPL");
				stats.posts++;
				stats.threads++;
				stats.posts+=replies;
				if(updateTime>topTime)
				{
					topTime=updateTime;
					topKey=key;
				}
				if(CMath.bset(attributes,JournalsLibrary.JournalEntry.ATTRIBUTE_STUCKY))
				{
					if(stats.stuckyKeys==null)
						stats.stuckyKeys=new Vector<String>();
					stats.stuckyKeys.add(key);
				}
			}
			R.close();
		}
		catch(Exception sqle)
		{
			Log.errOut("JournalLoader",sqle.getMessage());
		}
		if(topKey != null)
			stats.latestKey = topKey;
		else
			stats.latestKey = null;
		stats.imagePath="";
		stats.shortIntro="[This is the short journal description.]";
		stats.longIntro="[This is the long journal description.    To change it, use forum Admin, or create a journal entry addressed to JOURNALINTRO with updatetime 0.]";
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
		finally
		{
			if(D!=null) DB.DBDone(D);
		}
	}

	
	public void DBDelete(String Journal, String key)
	{
		Journal = DB.injectionClean(Journal);
		key = DB.injectionClean(key);
		
		if(Journal==null) return;
		synchronized(Journal.toUpperCase().intern())
		{
			if(key!=null)
			{
				DB.update("DELETE FROM CMJRNL WHERE CMJKEY='"+key+"' OR CMPART='"+key+"'");
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
		Journal = DB.injectionClean(Journal);
		key = DB.injectionClean(key);
		from = DB.injectionClean(from);
		to = DB.injectionClean(to);
		subject = DB.injectionClean(subject);
		message = DB.injectionClean(message);
		
		if(Journal==null) return;
		synchronized(Journal.toUpperCase().intern())
		{
			JournalsLibrary.JournalEntry entry=DBReadJournalEntry(Journal, key);
			if(entry==null) return;
			long now=System.currentTimeMillis();
			String oldkey=entry.key;
			String oldmsg=entry.msg;
			int replies = entry.replies+1;
			message=oldmsg+JournalsLibrary.JOURNAL_BOUNDARY
			 +"^yReply from^N: "+from+"%0D"
			 +"^yDate/Time ^N: "+CMLib.time().date2String(now)+"%0D"
			 +message;
			DB.update("UPDATE CMJRNL SET CMUPTM="+now+", CMMSGT='"+message+"', CMREPL="+replies+" WHERE CMJKEY='"+oldkey+"'");
		}
	}
	
	public void DBWrite(String Journal,
						String from, 
						String to, 
						String subject,
						String message)
	{
		Journal = DB.injectionClean(Journal);
		from = DB.injectionClean(from);
		to = DB.injectionClean(to);
		subject = DB.injectionClean(subject);
		message = DB.injectionClean(message);
		
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
		Journal = DB.injectionClean(Journal);
		entry.data = DB.injectionClean(entry.data);
		entry.from = DB.injectionClean(entry.from);
		entry.key = DB.injectionClean(entry.key);
		entry.msg = DB.injectionClean(entry.msg);
		entry.msgIcon = DB.injectionClean(entry.msgIcon);
		entry.parent = DB.injectionClean(entry.parent);
		entry.subj = DB.injectionClean(entry.subj);
		entry.to = DB.injectionClean(entry.to);
		
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
			+"CMIMGP, "
			+"CMVIEW, "
			+"CMREPL, "
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
			+",'"+entry.msgIcon
			+"',"+entry.views
			+","+entry.replies
			+",'"+entry.msg+"')");
			if((entry.parent!=null)&&(entry.parent.length()>0))
			{
				// this constitutes a threaded reply -- update the counter
				JournalsLibrary.JournalEntry parentEntry=DBReadJournalEntry(Journal, entry.parent);
				if(parentEntry!=null)
					DBUpdateMessageReplies(parentEntry.key,parentEntry.replies+1);
			}
			if(System.currentTimeMillis()==now) // ensures unique keys.
				try{Thread.sleep(1);}catch(Exception e){}
		}
	}
}
