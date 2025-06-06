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
   Copyright 2008-2025 Bo Zimmerman

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
public class GCClassLoader
{
	protected DBConnector DB=null;
	public GCClassLoader(final DBConnector newDB)
	{
		DB=newDB;
	}

	public List<DatabaseEngine.AckRecord> DBReadClasses()
	{
		DBConnection D=null;
		final Vector<DatabaseEngine.AckRecord> rows=new Vector<DatabaseEngine.AckRecord>();
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMCCAC");
			while(R.next())
			{
				final String ccid = DBConnections.getRes(R,"CMCCID");
				final String cdat = DBConnections.getRes(R,"CMCDAT");
				final DatabaseEngine.AckRecord ack = new DatabaseEngine.AckRecord()
				{
					@Override
					public String ID()
					{
						return ccid;
					}

					@Override
					public String data()
					{
						return cdat;
					}

					@Override
					public String typeClass()
					{
						return "GenCharClass";
					}
				};
				rows.addElement(ack);
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

	public void DBDeleteClass(final String classID)
	{
		DB.update("DELETE FROM CMCCAC WHERE CMCCID='"+classID+"'");
	}

	public void DBCreateClass(final String classID, final String data)
	{
		DB.updateWithClobs(
		 "INSERT INTO CMCCAC ("
		 +"CMCCID, "
		 +"CMCDAT "
		 +") values ("
		 +"'"+classID+"',"
		 +"?"
		 +")",
		 data+" ");
	}
}
