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
public class Goto extends At
{
	public Goto(){}

	private String[] access={"GOTO"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room room=null;
		if(commands.size()<2)
		{
			mob.tell("Go where? Try a Room ID, player name, area name, or room text!");
			return false;
		}
		commands.removeElementAt(0);
		StringBuffer cmd = new StringBuffer(CMParms.combine(commands,0));
		Vector stack=(Vector)Resources.getResource("GOTOS_FOR_"+mob.Name().toUpperCase());
		if(stack==null)
		{
			stack=new Vector();
			Resources.submitResource("GOTOS_FOR_"+mob.Name().toUpperCase(),stack);
		}
		else
		if(stack.size()>10)
			stack.removeElementAt(0);
		Room curRoom=mob.location();
		if("PREVIOUS".startsWith(cmd.toString().toUpperCase()))
		{
			if(stack.size()==0)
				mob.tell("Your previous room stack is empty.");
			else
			{
				room=CMLib.map().getRoom((String)stack.lastElement());
				stack.removeElementAt(stack.size()-1);
			}
		}
		else
			room=CMLib.map().findWorldRoomLiberally(mob,cmd.toString(),"RIPMA",100,120);

		if(room==null)
		{
			mob.tell("Goto where? Try a Room ID, player name, area name, room text, or PREVIOUS!");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,"GOTO"))
		{
			mob.tell("You aren't powerful enough to do that. Try 'GO'.");
			return false;
		}
		if(curRoom==room)
		{
			mob.tell("Done.");
			return false;
		}
		if(!"PREVIOUS".startsWith(cmd.toString().toUpperCase()))
		{
			if((stack.size()==0)||(stack.lastElement()!=mob.location()))
				stack.addElement(CMLib.map().getExtendedRoomID(mob.location()));
		}
		if(mob.playerStats().poofOut().length()>0)
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,mob.playerStats().poofOut());
		room.bringMobHere(mob,true);
		if(mob.playerStats().poofIn().length()>0)
			room.show(mob,null,CMMsg.MSG_OK_VISUAL,mob.playerStats().poofIn());
		CMLib.commands().postLook(mob,true);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"GOTO");}

	
}
