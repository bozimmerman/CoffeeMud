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
public class PollLoader
{
	protected DBConnector DB=null;
	public PollLoader(DBConnector newDB)
	{
		DB=newDB;
	}
    public Vector DBRead(String name)
    {
        DBConnection D=null;
        Vector V=new Vector();
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMPOLL WHERE CMNAME='"+name+"'");
            while(R.next())
            {
                V.addElement(DBConnections.getRes(R,"CMNAME"));
                V.addElement(DBConnections.getRes(R,"CMBYNM"));
                V.addElement(DBConnections.getRes(R,"CMSUBJ"));
                V.addElement(DBConnections.getRes(R,"CMDESC"));
                V.addElement(DBConnections.getRes(R,"CMOPTN"));
                V.addElement(Long.valueOf(DBConnections.getLongRes(R,"CMFLAG")));
                V.addElement(DBConnections.getRes(R,"CMQUAL"));
                V.addElement(DBConnections.getRes(R,"CMRESL"));
                V.addElement(Long.valueOf(DBConnections.getLongRes(R,"CMEXPI")));
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("PollLoader",sqle);
        }
        if(D!=null) DB.DBDone(D);
        // log comment
        return V;
    }

    
    public Vector DBReadList()
    {
        DBConnection D=null;
        Vector rows=new Vector();
        try
        {
            D=DB.DBFetch();
            ResultSet R=D.query("SELECT * FROM CMPOLL");
            while(R.next())
            {
                Vector V=new Vector();
                V.addElement(DBConnections.getRes(R,"CMNAME"));
                V.addElement(Long.valueOf(DBConnections.getLongRes(R,"CMFLAG")));
                V.addElement(DBConnections.getRes(R,"CMQUAL"));
                V.addElement(Long.valueOf(DBConnections.getLongRes(R,"CMEXPI")));
                rows.addElement(V);
            }
        }
        catch(Exception sqle)
        {
            Log.errOut("PollLoader",sqle);
        }
        if(D!=null) DB.DBDone(D);
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
        DB.update(
                "UPDATE CMPOLL SET"
                +" CMRESL='"+results+" '"
                +" WHERE CMNAME='"+OldName+"'");
        
        DB.update(
            "UPDATE CMPOLL SET"
            +"  CMNAME='"+name+"'"
            +", CMBYNM='"+player+"'"
            +", CMSUBJ='"+subject+"'"
            +", CMDESC='"+description+" '"
            +", CMOPTN='"+optionXML+" '"
            +", CMFLAG="+flag
            +", CMQUAL='"+qualZapper+"'"
            +", CMEXPI="+expiration
            +"  WHERE CMNAME='"+OldName+"'");

    }
    
    public void DBUpdate(String name,  String results)
    {
        DB.update(
        "UPDATE CMPOLL SET"
        +" CMRESL='"+results+" '"
        +" WHERE CMNAME='"+name+"'");
    }
    
    public void DBDelete(String name)
    {
        DB.update("DELETE FROM CMPOLL WHERE CMNAME='"+name+"'");
        try{Thread.sleep(500);}catch(Exception e){}
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
        DB.update(
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
         +"'"+description+"', "
         +"'"+optionXML+"',"
         +""+flag+","
         +"'"+qualZapper+"',"
         +"'"+results+" ',"
         +""+expiration+""
         +")");
    }
}
