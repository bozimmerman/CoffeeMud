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
public class Save extends StdCommand
{
	public Save(){}

	private String[] access={"SAVE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()==1)&&CMSecurity.isSaveFlag("NOPLAYERS"))
		{
		    if(!mob.isMonster())
		    {
				CMLib.database().DBUpdatePlayer(mob);
				CMLib.database().DBUpdateFollowers(mob);
				mob.tell("Your player record has been updated.");
		    }
		    return false;
		}
		
		String commandType="";
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		if(commands.size()>1)
			commandType=((String)commands.lastElement()).toUpperCase();
		
		if(commandType.equals("USERS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS"))
			{
				mob.tell("You are not allowed to save players.");
				return false;
			}
			for(int s=0;s<CMLib.sessions().size();s++)
			{
				Session session=CMLib.sessions().elementAt(s);
				MOB M=session.mob();
				if(M!=null)
				{
					CMLib.database().DBUpdatePlayer(M);
					CMLib.database().DBUpdateFollowers(M);
				}
			}
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes everyone.\n\r");
		}
		else
		if(commandType.equals("ITEMS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS"))
			{
				mob.tell("You are not allowed to save the mobs here.");
				return false;
			}
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("AREA")))
			{
				if((mob.session()!=null)&&(mob.session().confirm("Doing this assumes every item in every room in this area is correctly placed.  Are you sure (N/y)?","N")))
				{
					Area A=mob.location().getArea();
					for(Enumeration e=A.getProperMap();e.hasMoreElements();)
						CMLib.utensils().clearDebriAndRestart((Room)e.nextElement(),1);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
				}
				else
					return false;
			}
			else
			{
				CMLib.utensils().clearDebriAndRestart(mob.location(),1);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the room.\n\r");
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS"))
			{
				mob.tell("You are not allowed to save the contents here.");
				return false;
			}
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("AREA")))
			{
				if((mob.session()!=null)&&(mob.session().confirm("Doing this assumes every mob and item in every room in this area is correctly placed.  Are you sure (N/y)?","N")))
				{
					Area A=mob.location().getArea();
					for(Enumeration e=A.getProperMap();e.hasMoreElements();)
						CMLib.utensils().clearDebriAndRestart((Room)e.nextElement(),0);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
				}
				else
					return false;
			}
			else
			{
				CMLib.utensils().clearDebriAndRestart(mob.location(),0);
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the room.\n\r");
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(commandType.equals("MOBS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS"))
			{
				mob.tell("You are not allowed to save the mobs here.");
				return false;
			}
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("AREA")))
			{
				if((mob.session()!=null)&&(mob.session().confirm("Doing this assumes every mob in every room in this area is correctly placed.  Are you sure (N/y)?","N")))
				{
					Area A=mob.location().getArea();
					for(Enumeration e=A.getProperMap();e.hasMoreElements();)
						CMLib.utensils().clearDebriAndRestart((Room)e.nextElement(),2);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
				}
				else 
					return false;
				
			}
			else
			{
				CMLib.utensils().clearDebriAndRestart(mob.location(),2);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the room.\n\r");
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(commandType.equals("QUESTS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS"))
			{
				mob.tell("You are not allowed to save the contents here.");
				return false;
			}
			CMLib.quests().save();
			mob.tell("Quest list saved.");
		}
		else
		{
			mob.tell(
				"\n\rYou cannot save '"+commandType+"'. "
				+"However, you might try "
				+"ITEMS, USERS, QUESTS, MOBS, or ROOM.");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")
												 ||CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")
												 ||CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")
												 ||CMSecurity.isSaveFlag("NOPLAYERS");}

	
}
