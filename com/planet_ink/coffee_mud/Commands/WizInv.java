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
public class WizInv extends StdCommand
{
	public WizInv()
	{
	}

	private final String[]	access	= I(new String[] { "WIZINVISIBLE", "WIZINV", "NOWIZINV" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String str=commands.get(0);
		if(Character.toUpperCase(str.charAt(0))!='W')
			commands.add(1,"OFF");
		commands.remove(0);
		int abilityCode=PhyStats.IS_NOT_SEEN|PhyStats.IS_CLOAKED;
		Ability A=mob.fetchEffect("Prop_WizInvis");
		if((commands.size()>0)&&("NOCLOAK".startsWith(CMParms.combine(commands,0).trim().toUpperCase())))
			abilityCode=PhyStats.IS_NOT_SEEN;
		if(CMParms.combine(commands,0).trim().equalsIgnoreCase("OFF"))
		{
			if(A!=null)
				A.unInvoke();
			else
				mob.tell(L("You are not wizinvisible!"));
			return false;
		}
		else
		if(A!=null)
		{
			if(CMath.bset(A.abilityCode(),abilityCode))
			{
				mob.tell(L("You have already faded from view!"));
				return false;
			}
		}

		if(A==null)
			A=CMClass.getAbility("Prop_WizInvis");
		if(A!=null)
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fade(s) from view!"));
			if(mob.fetchEffect(A.ID())==null)
				mob.addPriorityEffect((Ability)A.copyOf());
			A=mob.fetchEffect(A.ID());
			if(A!=null)
			{
				A.setAbilityCode(abilityCode);
				A.setMiscText("UNINVOKABLE");
			}
			mob.recoverPhyStats();
			mob.location().recoverRoomStats();
			mob.tell(L("You may uninvoke WIZINV with 'WIZINV OFF'."));
			return false;
		}
		mob.tell(L("Wizard invisibility is not available!"));
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
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.WIZINV);
	}

}
