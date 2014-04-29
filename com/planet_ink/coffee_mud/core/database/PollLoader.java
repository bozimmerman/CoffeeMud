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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;


/*
   Copyright 2000-2014 Bo Zimmerman

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
public class PollLoader
{
	protected DBConnector DB=null;
	public PollLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	public DatabaseEngine.PollData DBRead(String name)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMPOLL WHERE CMNAME='"+name+"'");
			while(R.next())
			{
				final DatabaseEngine.PollData data = new DBInterface.PollData();
				data.name=DBConnections.getRes(R,"CMNAME");
				data.byName=DBConnections.getRes(R,"CMBYNM");
				data.subject=DBConnections.getRes(R,"CMSUBJ");
				data.description=DBConnections.getRes(R,"CMDESC");
				data.options=DBConnections.getRes(R,"CMOPTN");
				data.flag=DBConnections.getLongRes(R,"CMFLAG");
				data.qual=DBConnections.getRes(R,"CMQUAL");
				data.results=DBConnections.getRes(R,"CMRESL");
				data.expiration=DBConnections.getLongRes(R,"CMEXPI");
				return data;
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("PollLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return null;
	}


	public List<DatabaseEngine.PollData> DBReadList()
	{
		DBConnection D=null;
		final Vector<DatabaseEngine.PollData> rows=new Vector<DatabaseEngine.PollData>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMPOLL");
			while(R.next())
			{
				final DatabaseEngine.PollData data = new DBInterface.PollData();
				data.name=DBConnections.getRes(R,"CMNAME");
				data.flag=DBConnections.getLongRes(R,"CMFLAG");
				data.qual=DBConnections.getRes(R,"CMQUAL");
				data.expiration=DBConnections.getLongRes(R,"CMEXPI");
				rows.addElement(data);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("PollLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public void DBUpdate(String OldName,
								String name,
								String player,
								String subject,
								String description,
								String optionXML,
								int flag,
								String qualZapper,
								String results,
								long expiration)
	{
		DB.updateWithClobs(
				"UPDATE CMPOLL SET"
				+" CMRESL=?"
				+" WHERE CMNAME='"+OldName+"'", results+" ");

		DB.updateWithClobs(
			"UPDATE CMPOLL SET"
			+"  CMNAME='"+name+"'"
			+", CMBYNM='"+player+"'"
			+", CMSUBJ='"+subject+"'"
			+", CMDESC=?"
			+", CMOPTN=?"
			+", CMFLAG="+flag
			+", CMQUAL='"+qualZapper+"'"
			+", CMEXPI="+expiration
			+"  WHERE CMNAME='"+OldName+"'", new String[][]{{description+" ", optionXML+" "}});

	}

	public void DBUpdate(String name,  String results)
	{
		DB.updateWithClobs(
		"UPDATE CMPOLL SET"
		+" CMRESL=?"
		+" WHERE CMNAME='"+name+"'", results+" ");
	}

	public void DBDelete(String name)
	{
		DB.update("DELETE FROM CMPOLL WHERE CMNAME='"+name+"'");
		try{Thread.sleep(500);}catch(final Exception e){}
		if(DB.queryRows("SELECT * FROM CMPOLL WHERE CMNAME='"+name+"'")>0)
			Log.errOut("Failed to delete data from poll "+name+".");
	}

	public void DBCreate(String name,
								String player,
								String subject,
								String description,
								String optionXML,
								int flag,
								String qualZapper,
								String results,
								long expiration)
	{
		DB.updateWithClobs(
		 "INSERT INTO CMPOLL ("
		 +"CMNAME, "
		 +"CMBYNM, "
		 +"CMSUBJ, "
		 +"CMDESC, "
		 +"CMOPTN, "
		 +"CMFLAG, "
		 +"CMQUAL, "
		 +"CMRESL, "
		 +"CMEXPI "
		 +") values ("
		 +"'"+name+"',"
		 +"'"+player+"',"
		 +"'"+subject+"',"
		 +"?, "
		 +"?,"
		 +""+flag+","
		 +"'"+qualZapper+"',"
		 +"?,"
		 +""+expiration+""
		 +")", new String[][]{{description,optionXML,results+" "}});
	}
}
