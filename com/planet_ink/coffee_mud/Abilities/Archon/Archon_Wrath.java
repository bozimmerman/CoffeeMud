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

public class Archon_Wrath extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Wrath";
	}

	private final static String localizedName = CMLib.lang().L("Wrath");

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
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings = I(new String[] { "WRATH" });

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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		boolean announce=false;
		if(commands.size()>0)
		{
			if((commands.get(commands.size()-1)).equals("!"))
			{
				commands.remove(commands.size()-1);
				announce=true;
			}
		}
		final MOB target=getTargetAnywhere(mob,commands,givenTarget,true);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
									auto?L("<T-NAME> <T-IS-ARE> knocked out of <T-HIS-HER> shoes!!!"):
										 L("^F**<S-NAME> BLAST(S) <T-NAMESELF>**, knocking <T-HIM-HER> out of <T-HIS-HER> shoes!!^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(target.location().okMessage(mob,msg))
			{
				target.location().send(mob,msg);
				if(target.curState().getHitPoints()>2)
					target.curState().setHitPoints(target.curState().getHitPoints()/2);
				if(target.curState().getMana()>2)
					target.curState().setMana(target.curState().getMana()/2);
				if(target.curState().getMovement()>2)
					target.curState().setMovement(target.curState().getMovement()/2);
				final Item I=target.fetchFirstWornItem(Wearable.WORN_FEET);
				if(I!=null)
				{
					I.unWear();
					I.removeFromOwnerContainer();
					target.location().addItem(I,ItemPossessor.Expire.Player_Drop);
				}
				Log.sysOut("Banish",mob.Name()+" wrathed "+target.name()+".");
				if(announce)
				{
					final Command C=CMClass.getCommand("Announce");
					try
					{
						C.execute(mob,new XVector<String>("ANNOUNCE",target.name()+" is knocked out of "+target.charStats().hisher()+" shoes!!!"),MUDCmdProcessor.METAFLAG_FORCED);
					}
					catch (final Exception e)
					{
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to inflict <S-HIS-HER> wrath upon <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
