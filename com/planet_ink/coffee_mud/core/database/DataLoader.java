package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class DataLoader
{
	protected DBConnector DB=null;
	protected DatabaseEngine engine=null;
	
	public DataLoader(DatabaseEngine engine, DBConnector newDB)
	{
		this.DB=newDB;
		this.engine=engine;
	}
	
	public List<PlayerData> DBRead(String playerID, String section)
	{
		DBConnection D=null;
		final Vector<PlayerData> rows=new Vector<PlayerData>();
		try
		{
			D=DB.DBFetch();
			ResultSet R=null;
			playerID = DB.injectionClean(playerID);
			section = DB.injectionClean(section);
			R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'");
			while(R.next())
			{
				final String playerID2=DBConnections.getRes(R,"CMPLID");
				final String section2=DBConnections.getRes(R,"CMSECT");
				final PlayerData d = createPlayerData();
				d.who(playerID2);
				d.section(section2);
				d.key(DBConnections.getRes(R,"CMPKEY"));
				d.xml(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(d);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public List<PlayerData> DBReadAllPlayerData(String playerID)
	{
		DBConnection D=null;
		final Vector<PlayerData> rows=new Vector<PlayerData>();
		try
		{
			D=DB.DBFetch();
			playerID = DB.injectionClean(playerID);
			final ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'");
			while(R.next())
			{
				final String playerID2=DBConnections.getRes(R,"CMPLID");
				if(playerID2.equalsIgnoreCase(playerID))
				{
					final PlayerData d = createPlayerData();
					d.who(playerID2);
					d.section(DBConnections.getRes(R,"CMSECT"));
					d.key(DBConnections.getRes(R,"CMPKEY"));
					d.xml(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(d);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public int DBCount(String playerID, String section)
	{
		DBConnection D=null;
		int rows=0;
		try
		{
			D=DB.DBFetch();
			ResultSet R=null;
			playerID = DB.injectionClean(playerID);
			section = DB.injectionClean(section);
			R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'");
			rows = D.getRecordCount(R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public int DBCountBySection(String section)
	{
		DBConnection D=null;
		int rows=0;
		try
		{
			D=DB.DBFetch();
			ResultSet R=null;
			section = DB.injectionClean(section);
			R=D.query("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'");
			rows = D.getRecordCount(R);
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public List<String> DBReadAuthorsBySection(String section)
	{
		DBConnection D=null;
		List<String> authors = new ArrayList<String>();
		try
		{
			D=DB.DBFetch();
			ResultSet R=null;
			section = DB.injectionClean(section);
			R=D.query("SELECT CMPLID FROM CMPDAT WHERE CMSECT='"+section+"'");
			while(R.next())
			{
				final String plid=DBConnections.getRes(R,"CMPLID");
				if(!authors.contains(plid))
					authors.add(plid);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return authors;
	}

	public List<PlayerData> DBReadByKeyMask(String section, String keyMask)
	{
		DBConnection D=null;
		final Vector<PlayerData> rows=new Vector<PlayerData>();
		final Pattern P=Pattern.compile(keyMask, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		try
		{
			D=DB.DBFetch();
			section = DB.injectionClean(section);
			final ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'");
			while(R.next())
			{
				final String plid=DBConnections.getRes(R,"CMPLID");
				final String sect=DBConnections.getRes(R,"CMSECT");
				final String key=DBConnections.getRes(R,"CMPKEY");
				final Matcher M=P.matcher(key);
				if(M.find())
				{
					final PlayerData d = createPlayerData();
					d.who(plid);
					d.section(sect);
					d.key(key);
					d.xml(DBConnections.getRes(R,"CMPDAT"));
					rows.addElement(d);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public List<PlayerData> DBReadKey(String key)
	{
		DBConnection D=null;
		final Vector<PlayerData> rows=new Vector<PlayerData>();
		try
		{
			D=DB.DBFetch();
			key = DB.injectionClean(key);
			final ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPKEY='"+key+"'");
			while(R.next())
			{
				final String plid=DBConnections.getRes(R,"CMPLID");
				final String sect=DBConnections.getRes(R,"CMSECT");
				key=DBConnections.getRes(R,"CMPKEY");
				final PlayerData d = createPlayerData();
				d.who(plid);
				d.section(sect);
				d.key(key);
				d.xml(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(d);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public List<PlayerData> DBRead(String playerID, String section, String key)
	{
		DBConnection D=null;
		final Vector<PlayerData> rows=new Vector<PlayerData>();
		try
		{
			D=DB.DBFetch();
			ResultSet R=null;
			playerID = DB.injectionClean(playerID);
			section = DB.injectionClean(section);
			key = DB.injectionClean(key);
			R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"' AND CMPKEY='"+key+"'");
			while(R.next())
			{
				final String playerID2=DBConnections.getRes(R,"CMPLID");
				final String section2=DBConnections.getRes(R,"CMSECT");
				final PlayerData d = createPlayerData();
				d.who(playerID2);
				d.section(section2);
				d.key(DBConnections.getRes(R,"CMPKEY"));
				d.xml(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(d);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public List<PlayerData> DBRead(String section)
	{
		DBConnection D=null;
		final Vector<PlayerData> rows=new Vector<PlayerData>();
		try
		{
			D=DB.DBFetch();
			section = DB.injectionClean(section);
			final ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'");
			while(R.next())
			{
				final PlayerData d = createPlayerData();
				d.who(DBConnections.getRes(R,"CMPLID"));
				d.section(DBConnections.getRes(R,"CMSECT"));
				d.key(DBConnections.getRes(R,"CMPKEY"));
				d.xml(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(d);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public List<String> DBReadNames(String section)
	{
		DBConnection D=null;
		final TreeSet<String> found=new TreeSet<String>();
		final Vector<String> rows=new Vector<String>();
		try
		{
			D=DB.DBFetch();
			section = DB.injectionClean(section);
			final ResultSet R=D.query("SELECT CMPLID FROM CMPDAT WHERE CMSECT='"+section+"'");
			while(R.next())
			{
				final String name=DBConnections.getRes(R,"CMPLID");
				if(!found.contains(name))
				{
					found.add(name);
					rows.addElement(name);
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public List<PlayerData> DBRead(String playerID, List<String> sections)
	{
		DBConnection D=null;
		final Vector<PlayerData> rows=new Vector<PlayerData>();
		if((sections==null)||(sections.size()==0))
			return rows;
		try
		{
			D=DB.DBFetch();
			final StringBuffer orClause=new StringBuffer("");
			for(int i=0;i<sections.size();i++)
			{
				final String section = DB.injectionClean(sections.get(i));
				orClause.append("CMSECT='"+section+"' OR ");
			}
			final String clause=orClause.toString().substring(0,orClause.length()-4);
			playerID = DB.injectionClean(playerID);
			final ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND ("+clause+")");
			while(R.next())
			{
				final PlayerData d = createPlayerData();
				d.who(DBConnections.getRes(R,"CMPLID"));
				d.section(DBConnections.getRes(R,"CMSECT"));
				d.key(DBConnections.getRes(R,"CMPKEY"));
				d.xml(DBConnections.getRes(R,"CMPDAT"));
				rows.addElement(d);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public PlayerData DBReCreate(String name, String section, String key, String xml)
	{
		synchronized(("RECREATE"+key).intern())
		{
			DBConnection D=null;
			try
			{
				D=DB.DBFetch();
				key = DB.injectionClean(key);
				final ResultSet R=D.query("SELECT * FROM CMPDAT WHERE CMPKEY='"+key+"'");
				final boolean exists=R.next();
				DB.DBDone(D);
				D=null;
				if(exists)
				{
					final PlayerData d = createPlayerData();
					d.who(name);
					d.section(section);
					d.key(key);
					d.xml(xml);
					DBUpdate(key,xml);
					return d;
				}
				else
					return DBCreate(name,section,key,xml);
			}
			catch(final Exception sqle)
			{
				Log.errOut("DataLoader",sqle);
				return null;
			}
			finally
			{
				DB.DBDone(D);
			}
		}
	}

	public void DBUpdate(String key, String xml)
	{
		key = DB.injectionClean(key);
		DB.updateWithClobs("UPDATE CMPDAT SET CMPDAT=? WHERE CMPKEY='"+key+"'", xml);
	}

	public void DBDelete(String playerID, String section)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			playerID = DB.injectionClean(playerID);
			section = DB.injectionClean(section);
			D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'",0);
			CMLib.s_sleep(500);
			if(DB.queryRows("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"' AND CMSECT='"+section+"'")>0)
				Log.errOut("Failed to delete data for player "+playerID+".");
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBDeletePlayer(String playerID)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			D.update("DELETE FROM CMPDAT WHERE CMPLID='"+playerID+"'",0);
			CMLib.s_sleep(500);
			if(DB.queryRows("SELECT * FROM CMPDAT WHERE CMPLID='"+playerID+"'")>0)
				Log.errOut("Failed to delete data for player "+playerID+".");
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBDelete(String playerID, String section, String key)
	{

		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			playerID = DB.injectionClean(playerID);
			section = DB.injectionClean(section);
			key = DB.injectionClean(key);
			D.update("DELETE FROM CMPDAT WHERE CMPKEY='"+key+"' AND CMPLID='"+playerID+"' AND CMSECT='"+section+"'",0);
			CMLib.s_sleep(500);
			if(DB.queryRows("SELECT * FROM CMPDAT WHERE CMPKEY='"+key+"' AND CMPLID='"+playerID+"' AND CMSECT='"+section+"'")>0)
				Log.errOut("Failed to delete data for player "+playerID+".");
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBDelete(String section)
	{
		section = DB.injectionClean(section);
		DB.update("DELETE FROM CMPDAT WHERE CMSECT='"+section+"'");
		CMLib.s_sleep(500);
		if(DB.queryRows("SELECT * FROM CMPDAT WHERE CMSECT='"+section+"'")>0)
			Log.errOut("Failed to delete data from section "+section+".");
	}

	public PlayerData DBCreate(String playerID, String section, String key, String data)
	{
		playerID = DB.injectionClean(playerID);
		section = DB.injectionClean(section);
		key = DB.injectionClean(key);
		PlayerData pData = createPlayerData();
		pData.who(playerID);
		pData.section(section);
		pData.key(key);
		pData.xml(data);
		DB.updateWithClobs(
		 "INSERT INTO CMPDAT ("
		 +"CMPLID, "
		 +"CMSECT, "
		 +"CMPKEY, "
		 +"CMPDAT "
		 +") values ("
		 +"'"+playerID+"',"
		 +"'"+section+"',"
		 +"'"+key+"',"
		 +"?"
		 +")",
		 data+" ");
		return pData;
	}

	public void DBReadArtifacts()
	{
		final List<String> itemSet=DBReadNames("ARTIFACTS");
		for(int i=0;i<itemSet.size();i++)
		{
			final String itemID=itemSet.get(i);
			final Ability A=CMClass.getAbility("Prop_Artifact");
			if(A!=null)
			{
				A.setMiscText("BOOT;"+itemID);
				if(!CMLib.threads().isTicking(A,Tickable.TICKID_ITEM_BOUNCEBACK))
					CMLib.threads().startTickDown(A, Tickable.TICKID_ITEM_BOUNCEBACK,4);
			}
		}
	}
	
	public PlayerData createPlayerData()
	{
		return new PlayerData()
		{
			private String	who		= "";
			private String	section	= "";
			private String	key		= "";
			private String	xml		= "";

			@Override
			public String who()
			{
				return who;
			}

			@Override
			public PlayerData who(String who)
			{
				this.who = who;
				return this;
			}

			@Override
			public String section()
			{
				return section;
			}

			@Override
			public PlayerData section(String section)
			{
				this.section = section;
				return this;
			}

			@Override
			public String key()
			{
				return key;
			}

			@Override
			public PlayerData key(String key)
			{
				this.key = key;
				return this;
			}

			@Override
			public String xml()
			{
				return xml;
			}

			@Override
			public PlayerData xml(String xml)
			{
				this.xml = xml;
				return this;
			}
		};
	}
}
