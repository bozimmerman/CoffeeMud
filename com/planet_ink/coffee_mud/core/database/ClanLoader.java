package com.planet_ink.coffee_mud.core.database;
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


/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004-2010 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */
public class ClanLoader
{
	protected DBConnector DB=null;
	public ClanLoader(DBConnector newDB)
	{
		DB=newDB;
	}
    protected int currentRecordPos=1;
    protected int recordCount=0;

	public void updateBootStatus(String loading)
	{
		CMProps.setUpLowVar(CMProps.SYSTEM_MUDSTATUS,"Booting: Loading "+loading+" ("+currentRecordPos+" of "+recordCount+")");
	}

	public void DBRead()
	{
		DBConnection D=null;
	    Clan C=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCLAN");
			recordCount=DB.getRecordCount(D,R);
			while(R.next())
			{
				currentRecordPos=R.getRow();
				String name=DBConnections.getRes(R,"CMCLID");
				C=CMLib.clans().getClanType(CMath.s_int(DBConnections.getRes(R,"CMTYPE")));
				C.setName(name);
				C.setPremise(DBConnections.getRes(R,"CMDESC"));
				C.setAcceptanceSettings(DBConnections.getRes(R,"CMACPT"));
				C.setPolitics(DBConnections.getRes(R,"CMPOLI"));
				C.setRecall(DBConnections.getRes(R,"CMRCLL"));
				C.setDonation(DBConnections.getRes(R,"CMDNAT"));
				C.setStatus(CMath.s_int(DBConnections.getRes(R, "CMSTAT")));
				C.setMorgue(DBConnections.getRes(R,"CMMORG"));
				C.setTrophies(CMath.s_int(DBConnections.getRes(R, "CMTROP")));
				CMLib.clans().addClan(C);
		        updateBootStatus("Clans");
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("Clan",sqle);
		}
		if(D!=null) DB.DBDone(D);
		// log comment
	}

	public void DBUpdate(Clan C)
	{
		String str="UPDATE CMCLAN SET "
				+"CMDESC='"+C.getPremise()+"',"
				+"CMACPT='"+C.getAcceptanceSettings()+"',"
				+"CMPOLI='"+C.getPolitics()+"',"
				+"CMRCLL='"+C.getRecall()+"',"
				+"CMDNAT='"+C.getDonation()+"',"
				+"CMSTAT="+C.getStatus()+","
				+"CMMORG='"+C.getMorgue()+"',"
				+"CMTROP="+C.getTrophies()+""
				+" WHERE CMCLID='"+C.clanID()+"'";
		DB.update(str);
	}

	public void DBCreate(Clan C)
	{
		if(C.clanID().length()==0) return;
		String str="INSERT INTO CMCLAN ("
			+"CMCLID,"
			+"CMTYPE,"
			+"CMDESC,"
			+"CMACPT,"
			+"CMPOLI,"
			+"CMRCLL,"
			+"CMDNAT,"
			+"CMSTAT,"
			+"CMMORG,"
			+"CMTROP"
			+") values ("
			+"'"+C.clanID()+"',"
			+""+C.getType()+","
			+"'"+C.getPremise()+"',"
			+"'"+C.getAcceptanceSettings()+"',"
			+"'"+C.getPolitics()+"',"
			+"'"+C.getRecall()+"',"
			+"'"+C.getDonation()+"',"
			+""+C.getStatus()+","
			+"'"+C.getMorgue()+"',"
			+""+C.getTrophies()
			+")";
			DB.update(str);
	}

	public void DBDelete(Clan C)
	{
		DB.update("DELETE FROM CMCLAN WHERE CMCLID='"+C.clanID()+"'");
	}

}