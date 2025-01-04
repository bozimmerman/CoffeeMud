package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2022-2025 Bo Zimmerman

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
public class Pimping extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Pimping";
	}

	private final static String localizedName = CMLib.lang().L("Pimping");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"PIMP"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_CRIMINAL;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB johnM= null;
		if(commands.size()>1)
		{
			final String what=commands.get(commands.size()-1);
			final XVector<String> toWhomV = new XVector<String>(what);
			johnM=super.getTarget(mob, toWhomV, null);
			if(johnM != null)
			{
				commands.remove(commands.size()-1);
				if(johnM == mob)
				{
					commonTelL(mob,"You can't pimp to yourself.");
					return false;
				}
				if(!johnM.isPlayer())
				{
					commonTelL(mob,"You can't pimp to @x1.",johnM.Name());
					return false;
				}
			}
			else
			{
				commonTelL(mob,"Pimp whom to whom?");
				return false;
			}
		}
		else
		{
			commonTelL(mob,"Pimp whom to whom?");
			return false;
		}
		if(commands.size()==0)
		{
			commonTelL(mob,"Pimp whom?");
			return false;
		}

		final MOB slaveM=super.getTarget(mob, commands, givenTarget);
		if(slaveM == null)
			return false;
		if(!CMLib.flags().isASlave(slaveM, mob))
		{
			commonTelL(mob,slaveM,null,"<T-NAME> do(es)n't seem to be your slave.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
		{
			final CMMsg smsg=CMClass.getMsg(slaveM,johnM,this,CMMsg.MSG_NOISYMOVEMENT,""); // non-null req for law
			if(mob.location().okMessage(mob, smsg))
			{
				mob.location().send(mob,smsg);
				final CMMsg msg=CMClass.getMsg(mob,johnM,slaveM,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> pimp(s) <O-NAME> out to <T-NAME>."));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					final PrivateProperty P = CMLib.law().getPropertyRecord(slaveM);
					if(P != null)
					{
						final long expires = System.currentTimeMillis()
								+ ((CMProps.getTicksPerMudHour()-super.getXLEVELLevel(mob)) * CMProps.getTickMillis());
						P.setStat("PIMP", johnM.Name());
						P.setStat("PIMPEXPIRE", ""+expires);
						if(slaveM.amFollowing()!=null)
							CMLib.commands().postFollow(slaveM, null, true);
						CMLib.commands().postFollow(slaveM, johnM , false);
					}
					else
						mob.location().show(slaveM, null, CMMsg.MSG_OK_VISUAL, L("The trade fails."));
				}
			}
		}
		else
			beneficialWordsFizzle(mob,johnM,L("<S-NAME> <S-IS-ARE>n't able to strike a deal with <T-NAME>."));
		return true;
	}
}
