package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class ClanDonateSet extends BaseClanner
{
	public ClanDonateSet(){}

	private String[] access={"CLANDONATESET"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean skipChecks=mob.Name().equals(mob.getClanID());
		commands.setElementAt("clandonateset",0);

		Room R=mob.location();
		if(skipChecks)
			R=CMMap.getRoom(Util.combine(commands,1));
		else
		{
			commands.clear();
			commands.addElement("clandonateset");
			commands.addElement(CMMap.getExtendedRoomID(R));
		}

		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		else
		{
			Clan C=Clans.getClan(mob.getClanID());
			if(C==null)
			{
				mob.tell("There is no longer a clan called "+mob.getClanID()+".");
				return false;
			}
			if(C.getStatus()>Clan.CLANSTATUS_ACTIVE)
			{
				mob.tell("You cannot set a donation room.  Your "+C.typeName()+" does not have enough members to be considered active.");
				return false;
			}
			if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANDONATESET,false))
			{
				if(!CoffeeUtensils.doesOwnThisProperty(C.ID(),R))
				{
					mob.tell("Your "+C.typeName()+" does not own this room.");
					return false;
				}
				else
				{
					if(skipChecks||goForward(mob,C,commands,Clan.FUNC_CLANDONATESET,true))
					{
						C.setDonation(CMMap.getExtendedRoomID(R));
						C.update();
						mob.tell("Your "+C.typeName()+" donation is now set to "+R.roomTitle()+".");
						clanAnnounce(mob, "Your "+C.typeName()+" donation is now set to "+R.roomTitle()+".");
						return false;
					}
				}
			}
			else
			{
				mob.tell("You aren't in the right position to set your "+C.typeName()+"'s donation room.");
				return false;
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
