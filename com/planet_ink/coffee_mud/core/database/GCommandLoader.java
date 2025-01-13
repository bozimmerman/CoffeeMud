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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class GCommandLoader
{
	protected DBConnector DB=null;
	public GCommandLoader(final DBConnector newDB)
	{
		DB=newDB;
	}

	public List<DatabaseEngine.AckRecord> DBReadCommands()
	{
		DBConnection D=null;
		final Vector<DatabaseEngine.AckRecord> rows=new Vector<DatabaseEngine.AckRecord>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCDAC");
			while(R.next())
			{
				final String cdid = DBConnections.getRes(R,"CMCDID");
				final String cdat = DBConnections.getRes(R,"CMCDAT");
				final String cdac = DBConnections.getRes(R,"CMCDCL");
				rows.addElement(new DatabaseEngine.AckRecord()
				{
					@Override
					public String ID()
					{
						return cdid;
					}

					@Override
					public String data()
					{
						return cdat;
					}

					@Override
					public String typeClass()
					{
						return cdac;
					}
				});
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("GCmdLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return rows;
	}

	public void DBCreateCommand(final String classID, final String baseClass, final String data)
	{
		DB.updateWithClobs(
		 "INSERT INTO CMCDAC ("
		 +"CMCDID, "
		 +"CMCDAT, "
		 +"CMCDCL "
		 +") values ("
		 +"'"+classID+"',"
		 +"?,"
		 +"'"+baseClass+"'"
		 +")",
		 data+" ");
	}

	public DatabaseEngine.AckRecord DBDeleteCommand(String classID)
	{
		DBConnection D=null;
		classID = DB.injectionClean(classID);
		DatabaseEngine.AckRecord ack=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCDAC WHERE CMCDID='"+classID+"'");
			while(R.next())
			{
				final String cdid = DBConnections.getRes(R,"CMCDID");
				final String cdat = DBConnections.getRes(R,"CMCDAT");
				final String cdac = DBConnections.getRes(R,"CMCDCL");
				ack = new DatabaseEngine.AckRecord()
				{
					@Override
					public String ID()
					{
						return cdid;
					}

					@Override
					public String data()
					{
						return cdat;
					}

					@Override
					public String typeClass()
					{
						return cdac;
					}
				};
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("GCmdLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		DB.update("DELETE FROM CMCDAC WHERE CMCDID='"+classID+"'");
		return ack;
	}
}
