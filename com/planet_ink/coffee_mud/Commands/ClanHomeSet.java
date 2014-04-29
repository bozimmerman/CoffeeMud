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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class ClanHomeSet extends StdCommand
{
	public ClanHomeSet(){}

	private final String[] access={"CLANHOMESET"};
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final String clanName=(commands.size()>1)?CMParms.combine(commands,1,commands.size()):"";

		Clan C=null;
		final boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks) C=mob.getClanRole(mob.Name()).first;

		if(C==null)
		for(final Pair<Clan,Integer> c : mob.clans())
			if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
			&&(c.first.getAuthority(c.second.intValue(), Clan.Function.SET_HOME)!=Authority.CAN_NOT_DO))
			{	C=c.first; break; }

		commands.setElementAt(getAccessWords()[0],0);

		Room R=mob.location();
		if(skipChecks)
			R=CMLib.map().getRoom(CMParms.combine(commands,1));
		else
		{
			commands.clear();
			commands.addElement(getAccessWords()[0]);
			commands.addElement(CMLib.map().getExtendedRoomID(R));
		}

		if(C==null)
		{
			mob.tell("You aren't allowed to set a home room for "+((clanName.length()==0)?"anything":clanName)+".");
			return false;
		}

		if(C.getStatus()>Clan.CLANSTATUS_ACTIVE)
		{
			mob.tell("You cannot set a home.  Your "+C.getGovernmentName()+" does not have enough members to be considered active.");
			return false;
		}
		if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.SET_HOME,false))
		{
			if(!CMLib.law().doesOwnThisProperty(C.clanID(),R))
			{
				mob.tell("Your "+C.getGovernmentName()+" does not own this room.");
				return false;
			}
			if(skipChecks||CMLib.clans().goForward(mob,C,commands,Clan.Function.SET_HOME,true))
			{
				C.setRecall(CMLib.map().getExtendedRoomID(R));
				C.update();
				mob.tell("The "+C.getGovernmentName()+" "+C.clanID()+" home is now set to "+R.displayText(mob)+".");
				CMLib.clans().clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" home is now set to "+R.displayText(mob)+".");
				return true;
			}
		}
		else
		{
			mob.tell("You aren't in the right position to set your "+C.getGovernmentName()+"'s home.");
			return false;
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return false;}


}
