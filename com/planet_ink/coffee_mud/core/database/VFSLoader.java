package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.CMFile.CMVFSFile;
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
   Copyright 2005-2018 Bo Zimmerman

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
public class VFSLoader
{
	protected DBConnector DB=null;
	public VFSLoader(DBConnector newDB)
	{
		DB=newDB;
	}

	public CMFile.CMVFSDir DBReadDirectory()
	{
		DBConnection D=null;
		final CMFile.CMVFSDir root=new CMFile.CMVFSDir(null,"");
		try
		{
			D=DB.DBFetch();
			if(D==null)
			{
				return null;
			}
			final ResultSet R=D.query("SELECT * FROM CMVFS");
			while(R.next())
			{
				final String fname = DBConnections.getRes(R,"CMFNAM");
				final int mask = (int)DBConnections.getLongRes(R,"CMDTYP");
				final long time = DBConnections.getLongRes(R,"CMMODD");
				final String author = DBConnections.getRes(R,"CMWHOM");
				root.add(new CMFile.CMVFSFile(fname,mask,time,author));
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("VFSLoader",sqle);
			return null;
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return root;
	}

	public CMFile.CMVFSFile DBRead(String filename)
	{
		DBConnection D=null;
		CMFile.CMVFSFile row = null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT * FROM CMVFS WHERE CMFNAM='"+filename+"'");
			if(R.next())
			{
				final String possFName=DBConnections.getRes(R,"CMFNAM");
				if(possFName.equalsIgnoreCase(filename))
				{
					final int bits=(int)DBConnections.getLongRes(R,"CMDTYP");
					final long mod=DBConnections.getLongRes(R,"CMMODD");
					final String author = DBConnections.getRes(R,"CMWHOM");
					final String data=DBConnections.getRes(R,"CMDATA");
					row = new CMFile.CMVFSFile(filename,bits,mod,author);
					row.setData(B64Encoder.B64decode(data));
				}
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("VFSLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
		// log comment
		return row;
	}

	private String makeVBuf(Object data)
	{
		String buf=null;
		if(data==null)
			buf="";
		else
		if(data instanceof String)
			buf=B64Encoder.B64encodeBytes(CMStrings.strToBytes((String)data));
		else
		if(data instanceof StringBuffer)
			buf=B64Encoder.B64encodeBytes(CMStrings.strToBytes(((StringBuffer)data).toString()));
		else
		if(data instanceof byte[])
			buf=B64Encoder.B64encodeBytes((byte[])data);
		return buf;
	}

	public void DBCreate(String filename, int bits, String creator, long updateTime, Object data)
	{
		final String buf=makeVBuf(data);
		if(buf==null)
		{
			Log.errOut("VFSLoader","Unable to save "+filename+" due to illegal data type: "+data.getClass().getName());
			return;
		}
		DB.updateWithClobs(
		 "INSERT INTO CMVFS ("
		 +"CMFNAM, "
		 +"CMDTYP, "
		 +"CMMODD, "
		 +"CMWHOM, "
		 +"CMDATA"
		 +") values ("
		 +"'"+filename+"',"
		 +""+(bits&CMFile.VFS_MASK_MASKSAVABLE)+","
		 +""+updateTime+","
		 +"'"+creator+"',"
		 +"?"
		 +")", buf);
	}

	public void DBUpSert(String filename, int bits, String creator, long updateTime, Object data)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			final ResultSet R=D.query("SELECT CMFNAM FROM CMVFS WHERE CMFNAM='"+filename+"'");
			if(!R.next())
			{
				R.close();
				DB.DBDone(D);
				D=null;
				DBCreate(filename, bits, creator, updateTime, data);
			}
			else
			{
				R.close();
				final String buf=makeVBuf(data);
				if(buf==null)
				{
					Log.errOut("VFSLoader","Unable to save "+filename+" due to illegal data type: "+data.getClass().getName());
					return;
				}
				DB.updateWithClobs(
						 "UPDATE CMVFS SET " +
						 "CMDTYP="+(bits&CMFile.VFS_MASK_MASKSAVABLE)+", " +
						 "CMMODD="+updateTime+","+
						 "CMWHOM='"+creator+"', "+
						 "CMDATA=? WHERE CMFNAM='"+filename+"'", buf);
			}
		}
		catch(final Exception sqle)
		{
			Log.errOut("VFSLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

	public void DBDelete(String filename)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			D.update("DELETE FROM CMVFS WHERE CMFNAM='"+filename+"'",0);
			CMLib.s_sleep(500);
			if(DB.queryRows("SELECT * FROM CMVFS WHERE CMFNAM='"+filename+"'")>0)
				Log.errOut("Failed to delete virtual file "+filename+".");
		}
		catch(final Exception sqle)
		{
			Log.errOut("VFSLoader",sqle);
		}
		finally
		{
			DB.DBDone(D);
		}
	}

}
