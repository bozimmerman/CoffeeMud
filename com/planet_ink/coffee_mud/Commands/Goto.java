package com.planet_ink.coffee_mud.Commands;
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

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Goto extends At
{
	public Goto(){}

	private final String[] access=I(new String[]{"GOTO"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Room room=null;
		if(commands.size()<2)
		{
			mob.tell(L("Go where? Try a Room ID, player name, area name, or room text!"));
			return false;
		}
		commands.remove(0);
		final StringBuffer cmd = new StringBuffer(CMParms.combine(commands,0));
		@SuppressWarnings("unchecked")
		List<String> stack=(List<String>)Resources.getResource("GOTOS_FOR_"+mob.Name().toUpperCase());
		if(stack==null)
		{
			stack=new Vector<String>();
			Resources.submitResource("GOTOS_FOR_"+mob.Name().toUpperCase(),stack);
		}
		else
		if(stack.size()>10)
			stack.remove(0);
		final Room curRoom=mob.location();
		room=CMLib.map().getRoom(cmd.toString());
		if(room == null)
		{
			if("PARENT".startsWith(cmd.toString().toUpperCase()))
			{
				if(mob.location().getGridParent()!=null)
					room=mob.location().getGridParent();
				else
					mob.tell(L("This room is not a grid child."));
			}
			else
			if("PREVIOUS".startsWith(cmd.toString().toUpperCase()))
			{
				if(stack.size()==0)
					mob.tell(L("Your previous room stack is empty."));
				else
				{
					room=CMLib.map().getRoom(stack.get(stack.size()-1));
					stack.remove(stack.size()-1);
				}
			}
			else
			if(CMLib.map().findArea(cmd.toString())!=null)
				room=CMLib.map().findArea(cmd.toString()).getRandomProperRoom();
			else
			if(cmd.toString().toUpperCase().startsWith("AREA "))
				room=CMLib.map().findAreaRoomLiberally(mob,curRoom.getArea(),CMParms.combine(commands,1),"RIPM",100);
			else
				room=CMLib.map().findWorldRoomLiberally(mob,cmd.toString(),"RIPMA",100,120000);
		}

		if(room==null)
		{
			mob.tell(L("Goto where? Try a Room ID, player name, area name, room text, or PREVIOUS!"));
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.GOTO))
		{
			mob.tell(L("You aren't powerful enough to do that. Try 'GO'."));
			return false;
		}
		if(curRoom==room)
		{
			mob.tell(L("Done."));
			return false;
		}
		if(!"PREVIOUS".startsWith(cmd.toString().toUpperCase()))
		{
			if((stack.size()==0)||(stack.get(stack.size()-1)!=mob.location().roomID()))
				stack.add(CMLib.map().getExtendedRoomID(mob.location()));
		}
		if(mob.playerStats().getPoofOut().length()>0)
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,mob.playerStats().getPoofOut());
		room.bringMobHere(mob,true);
		if(mob.playerStats().getPoofIn().length()>0)
			room.show(mob,null,CMMsg.MSG_OK_VISUAL,mob.playerStats().getPoofIn());
		CMLib.commands().postLook(mob,true);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedAnywhere(mob,CMSecurity.SecFlag.GOTO);
	}

}
