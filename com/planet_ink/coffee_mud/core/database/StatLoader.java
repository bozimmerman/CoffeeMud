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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class StatLoader
{
	protected DBConnector DB=null;
	public StatLoader(DBConnector newDB)
	{
		DB=newDB;
	}

	public CoffeeTableRow DBRead(long startTime)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMSTAT)))
			Log.debugOut("StatLoader","Reading content of Stat  "+CMLib.time().date2String(startTime));
		DBConnection D=null;
		CoffeeTableRow T=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMSTAT WHERE CMSTRT="+startTime);
			if(R.next())
			{
				T=(CoffeeTableRow)CMClass.getCommon("DefaultCoffeeTableRow");
				final long endTime=DBConnections.getLongRes(R,"CMENDT");
				final String data=DBConnections.getRes(R,"CMDATA");
				T.populate(startTime,endTime,data);
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
		return T;
	}

	public List<CoffeeTableRow> DBReadAfter(long startTime, long endTime)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMSTAT)))
			Log.debugOut("StatLoader","Reading content of Stats since "+CMLib.time().date2String(startTime));
		DBConnection D=null;
		CoffeeTableRow T=null;
		final List<CoffeeTableRow> rows=new Vector<CoffeeTableRow>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMSTAT WHERE CMSTRT>"+startTime);
			try
			{
				while(R.next())
				{
					T=(CoffeeTableRow)CMClass.getCommon("DefaultCoffeeTableRow");
					final long strTime=DBConnections.getLongRes(R,"CMSTRT");
					final long enTime=DBConnections.getLongRes(R,"CMENDT");
					if((endTime!=0)
					&&(endTime > strTime)
					&&(endTime >= enTime))
						break;
					final String data=DBConnections.getRes(R,"CMDATA");
					T.populate(strTime,endTime,data);
					rows.add(T);
				}
			}
			finally
			{
				R.close();
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

	public void DBDelete(long startTime)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMSTAT)))
			Log.debugOut("StatLoader","Deleting Stat  "+CMLib.time().date2String(startTime));
		try
		{
			DB.update("DELETE FROM CMSTAT WHERE CMSTRT="+startTime);
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
	}

	public boolean DBUpdate(long startTime, String data)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMSTAT)))
			Log.debugOut("StatLoader","Updating Stat  "+CMLib.time().date2String(startTime));
		int result=-1;
		try
		{
			result=DB.updateWithClobs("UPDATE CMSTAT SET CMDATA=? WHERE CMSTRT="+startTime, data);
		}
		catch(final Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		return (result != -1);
	}

	public boolean DBCreate(long startTime, long endTime, String data)
	{
		if(Log.debugChannelOn()&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.CMSTAT)))
			Log.debugOut("StatLoader","Creating Stat  "+CMLib.time().date2String(startTime));
		final int result = DB.updateWithClobs(
		 "INSERT INTO CMSTAT ("
		 +"CMSTRT, "
		 +"CMENDT, "
		 +"CMDATA "
		 +") values ("
		 +""+startTime+","
		 +""+endTime+","
		 +"?"
		 +")", data);
		return (result != -1);
	}
}
