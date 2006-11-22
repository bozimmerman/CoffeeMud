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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2006 Bo Zimmerman

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
public class After extends StdCommand implements Tickable
{
	public String name(){return "After";}
	public long getTickStatus(){return Tickable.STATUS_NOT;}

	public Vector afterCmds=new Vector();

	public After(){}

	private String[] access={getScr("After","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean every=false;
		commands.removeElementAt(0);

		String afterErr=getScr("After","afterErr");
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("stop"))
		{
			afterCmds.clear();
			CMLib.threads().deleteTick(this,Tickable.TICKID_AREA);
			mob.tell(getScr("After","ok"));
			return false;
		}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("list"))
		{
			//afterCmds.clear();
			int s=0;
			StringBuffer str=new StringBuffer("^xCurrently scheduled AFTERs: ^?^.^?\n\r");
			str.append(CMStrings.padRight("Next run",20)+" "+CMStrings.padRight(" Interval",20)+" "+CMStrings.padRight("Who",10)+" Command\n\r");
			while(s<afterCmds.size())
			{
				Vector V=(Vector)afterCmds.elementAt(s);
				long start=((Long)V.elementAt(0)).longValue();
				long duration=((Long)V.elementAt(1)).longValue();
				every=((Boolean)V.elementAt(2)).booleanValue();
				MOB M=((MOB)V.elementAt(3));
				Vector command=(Vector)V.elementAt(4);
				str.append(CMStrings.padRight(CMLib.time().date2String(start+duration),20)+" ");
				str.append((every?"*":" ")+CMStrings.padRight(CMLib.english().returnTime(duration,0),20)+" ");
				str.append(CMStrings.padRight(M.Name(),10)+" ");
				str.append(CMStrings.limit(CMParms.combine(command,0),25)+"\n\r");
				s++;
			}
			mob.tell(str.toString());
			return false;
		}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("every"))
		{ every=true; commands.removeElementAt(0);}
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		long time=CMath.s_long((String)commands.elementAt(0));
		if(time==0) { mob.tell(getScr("After","timerr")+afterErr); return false;}
		commands.removeElementAt(0);
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		String s=(String)commands.elementAt(0);
		if(s.equalsIgnoreCase(getScr("After","ticks")))
			time=time*Tickable.TIME_TICK;
		else
		if(s.equalsIgnoreCase(getScr("After","seconds")))
			time=time*1000;
		else
		if(s.equalsIgnoreCase(getScr("After","minutes")))
			time=time*1000*60;
		else
		if(s.equalsIgnoreCase(getScr("After","hours")))
			time=time*1000*60*60;
		else
		{
			mob.tell("'"+s+" "+getScr("After","timerr")+" "+afterErr);
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		Vector V=new Vector();
		V.addElement(new Long(System.currentTimeMillis()));
		V.addElement(new Long(time));
		V.addElement(new Boolean(every));
		V.addElement(mob);
		V.addElement(commands);
		afterCmds.addElement(V);
		CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
		mob.tell(getScr("After","ok"));
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"AFTER");}

	

	public boolean tick(Tickable ticking, int tickID)
	{
		if(afterCmds.size()==0) return false;
		int s=0;
		while(s<afterCmds.size())
		{
			Vector V=(Vector)afterCmds.elementAt(s);
			long start=((Long)V.elementAt(0)).longValue();
			long duration=((Long)V.elementAt(1)).longValue();
			if(System.currentTimeMillis()>(start+duration))
			{
				boolean every=((Boolean)V.elementAt(2)).booleanValue();
				MOB mob=((MOB)V.elementAt(3));
				Vector command=(Vector)V.elementAt(4);
				if(every)
				{
					V.setElementAt(new Long(System.currentTimeMillis()),0);
					s++;
				}
				else
					afterCmds.removeElementAt(s);
				mob.doCommand((Vector)command.clone());
			}
			else
				s++;
		}
		return true;
	}
}
