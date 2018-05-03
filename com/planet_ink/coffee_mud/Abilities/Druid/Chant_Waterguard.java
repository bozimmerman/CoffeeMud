package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2016-2018 Bo Zimmerman

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

public class Chant_Waterguard extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Waterguard";
	}

	private final static String	localizedName	= CMLib.lang().L("Waterguard");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Waterguard)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PRESERVING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	protected volatile int amountLeft = 0;
	
	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your waterguard fades."));

		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(!mob.amDead())
		&&(msg.targetMinor()==CMMsg.TYP_WATER)
		&&(msg.targetMajor(CMMsg.MASK_MALICIOUS))
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(invoker(),0,false))
		&&(msg.source().location()!=null)
		&&(amountLeft>0))
		{
			msg.source().location().show(msg.source(),affected,CMMsg.MSG_OK_VISUAL,L("<T-NAME> resist(s) the wet attack."));
			if(msg.sourceMinor()==CMMsg.TYP_DAMAGE)
			{
				amountLeft-=msg.value()/2;
				msg.setValue(msg.value()/2);
			}
			else
				msg.setValue(1);
		}
		else
		if((msg.target() instanceof Item)
		&&(msg.targetMinor()==CMMsg.TYP_WATER)
		&&(mob.isMine(msg.target())))
		{
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=super.getTarget(mob, commands, givenTarget, false, false);
		if(target == null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"":"^S<S-NAME> chant(s) for a waterproof field to envelop <T-NAME>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Chant_Waterguard guard = (Chant_Waterguard)beneficialAffect(mob,target,asLevel,0);
				if(guard!=null)
					guard.amountLeft = adjustedLevel(mob,asLevel)+(2*super.getXLEVELLevel(mob));
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s), but nothing happens."));

		return success;
	}
}
