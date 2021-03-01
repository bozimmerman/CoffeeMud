package com.planet_ink.coffee_mud.Abilities.Archon;
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
   Copyright 2020-2021 Bo Zimmerman

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
public class Archon_InstanceDisplay extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_InstanceDisplay";
	}

	private final static String localizedName = CMLib.lang().L("Instance Display");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings = I(new String[] { "INSTANCEDISPLAY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Instance information for which type id, and for whom?"));
			return false;
		}
		final String instanceID = commands.remove(0);
		MOB target=CMLib.players().getLoadPlayer(commands.get(0));

		if(target==null)
			target=CMLib.players().getPlayerAllHosts(commands.get(0));
		if(target==null)
		{
			mob.tell(L("@x1 is not a player.",commands.get(0)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		final Ability A=CMClass.getAbility("InstanceArea");
		final String disp;
		if(A==null)
			disp="";
		else
		{
			try
			{
				disp = A.getStat("SELECT-DISPLAYFOR \""+target.Name()+"\" \""+instanceID+"\"");
			}
			catch(final IllegalArgumentException ae)
			{
				mob.tell(L("'@x1' is not a valid instance id type."));
				return false;
			}
		}

		success = success && (disp.length()>0);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.TYP_JUSTICE,L("^F<S-NAME> divine(s) stuff about <T-NAMESELF> instancely!^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(A==null)
					mob.tell(L("Yea, sorry, that's broken."));
				else
					mob.tell(L("You believe they would be sent to: "+disp));
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to divine instance stuff about <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
