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
public class Goto extends At
{
	public Goto(){}

	private String[] access={"GOTO"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Room room=null;
		if(commands.size()<2)
		{
			mob.tell("Go where? Try a Room ID, player name, area name, or room text!");
			return false;
		}
		commands.removeElementAt(0);
		StringBuffer cmd = new StringBuffer(Util.combine(commands,0));
		Room curRoom=mob.location();
		room=findRoomLiberally(mob,cmd);

		if(room==null)
		{
			mob.tell("Goto where? Try a Room ID, player name, area name, or room text!");
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
		if(mob.playerStats().poofOut().length()>0)
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,mob.playerStats().poofOut());
		room.bringMobHere(mob,true);
		if(mob.playerStats().poofIn().length()>0)
			room.show(mob,null,CMMsg.MSG_OK_VISUAL,mob.playerStats().poofIn());
		CommonMsgs.look(mob,true);
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"GOTO");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
