package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public String ID(){return "SysOpSkills";}
	public String name(){return "SysOpSkills";}
	public long getTickStatus(){return Tickable.STATUS_NOT;}

	public Vector afterCmds=new Vector();

	public After(){}

	private String[] access={"AFTER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean every=false;
		commands.removeElementAt(0);

		String afterErr="format: after (every) [X] [TICKS/MINUTES/SECONDS/HOURS] [COMMAND]";
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("stop"))
		{
			afterCmds.clear();
			CMClass.ThreadEngine().deleteTick(this,MudHost.TICK_AREA);
			mob.tell("Ok.");
			return false;
		}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("every"))
		{ every=true; commands.removeElementAt(0);}
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		long time=Util.s_long((String)commands.elementAt(0));
		if(time==0) { mob.tell(" Time may not be 0. "+afterErr); return false;}
		commands.removeElementAt(0);
		if(commands.size()==0){ mob.tell(afterErr); return false;}
		String s=(String)commands.elementAt(0);
		if(s.equalsIgnoreCase("ticks"))
			time=time*MudHost.TICK_TIME;
		else
		if(s.equalsIgnoreCase("seconds"))
			time=time*1000;
		else
		if(s.equalsIgnoreCase("minutes"))
			time=time*1000*60;
		else
		if(s.equalsIgnoreCase("hours"))
			time=time*1000*60*60;
		else
		{
			mob.tell("'"+s+"' is not a valid time interval. "+afterErr);
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
		CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_AREA,1);
		mob.tell("Ok.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"AFTER");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

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
