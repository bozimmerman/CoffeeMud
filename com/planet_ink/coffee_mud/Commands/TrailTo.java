package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class TrailTo extends StdCommand
{
	public TrailTo(){}

	private String[] access={"TRAILTO"};
	public String[] getAccessWords(){return access;}


	public String trailTo(Room R1, Vector commands)
	{
		int radius=Integer.MAX_VALUE;
        HashSet<Room> ignoreRooms=null;
        TrackingLibrary.TrackingFlags flags = new TrackingLibrary.TrackingFlags();
        for(int c=0;c<commands.size();c++)
        {
            String s=(String)commands.elementAt(c);
            if(s.toUpperCase().startsWith("RADIUS"))
            {
                s=s.substring(("RADIUS").length()).trim();
                if(!s.startsWith("=")) continue;
                s=s.substring(1);
                commands.removeElementAt(c);
                radius=CMath.s_int(s);
            }
            else
            if(s.toUpperCase().startsWith("IGNOREROOMS"))
            {
                s=s.substring(("IGNOREROOMS").length()).trim();
                if(!s.startsWith("=")) continue;
                s=s.substring(1);
                commands.removeElementAt(c);
                Vector roomList=CMParms.parseCommas(s,true);
                ignoreRooms=new HashSet();
                for(int v=0;v<roomList.size();v++)
                {
                    Room R=CMLib.map().getRoom((String)roomList.elementAt(v));
                    if(R==null){ return "Ignored room "+((String)roomList.elementAt(v))+" is unknown!";}
                    if(!ignoreRooms.contains(R))ignoreRooms.add(R);
                }
            }
            else
            if(s.toUpperCase().startsWith("NOHOME"))
            {
                commands.removeElementAt(c);
                flags.add(TrackingLibrary.TrackingFlag.NOHOMES);
            }
        }
		String where=CMParms.combine(commands,1);
		if(where.length()==0) return "Trail to where? Try a Room ID, 'everyroom', or 'everyarea'.  You can also use the 'areanames', 'nohomes', 'ignorerooms=', and 'confirm!' flags.";
		if(R1==null) return "Where are you?";
		boolean confirm=false;
        boolean areaNames=false;
        boolean justTheFacts=false;
        if(where.toUpperCase().endsWith(" AREANAMES"))
        {
            where=where.substring(0,where.length()-10).trim();
            areaNames=true;
        }
        if(where.toUpperCase().endsWith(" JUSTTHEFACTS"))
        {
            where=where.substring(0,where.length()-13).trim();
            justTheFacts=true;
        }
		if(where.toUpperCase().endsWith(" CONFIRM!"))
		{
			where=where.substring(0,where.length()-9).trim();
			confirm=true;
		}
		Vector<Room> set=new Vector<Room>();
		CMLib.tracking().getRadiantRooms(R1,set,flags,null,radius,ignoreRooms);
		if(where.equalsIgnoreCase("everyarea"))
		{
			StringBuffer str=new StringBuffer("");
			for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				String trail = CMLib.tracking().getTrailToDescription(R1,set,A.name(),areaNames,confirm,radius,ignoreRooms,5);
				str.append(CMStrings.padRightPreserve(A.name(),30)+": "+trail+"\n\r");
			}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		if(where.equalsIgnoreCase("everyroom"))
		{
			StringBuffer str=new StringBuffer("");
			try
			{
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((R!=R1)&&(R.roomID().length()>0))
					{
						String trail = CMLib.tracking().getTrailToDescription(R1,set,R.roomID(),areaNames,confirm,radius,ignoreRooms,5);
						str.append(CMStrings.padRightPreserve(R.roomID(),30)+": "+trail+"\n\r");
					}
				}
		    }catch(NoSuchElementException nse){}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		{
			String str=CMLib.tracking().getTrailToDescription(R1,set,where,areaNames,confirm,radius,ignoreRooms,5);
			if(!justTheFacts)str=CMStrings.padRightPreserve(where,30)+": "+str;
			if(confirm) Log.rawSysOut(str);
			return str;
		}
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>0)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")))
		{
			commands.removeElementAt(commands.size()-1);
			commands.setElementAt(trailTo(mob.location(),commands),0);
		}
		else
		if(!mob.isMonster())
			mob.session().rawPrintln(trailTo(mob.location(),commands));
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"TRAILTO");}


}
