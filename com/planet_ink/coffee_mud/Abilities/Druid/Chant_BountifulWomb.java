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
   Copyright 2003-2024 Bo Zimmerman

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
public class Chant_BountifulWomb extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_BountifulWomb";
	}

	private final static String localizedName = CMLib.lang().L("Bountiful Womb");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Bountiful Womb)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("Your bountiful womb subsides."));
	}

	private boolean done=false;
	private int chance=-1;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((ticking instanceof MOB)
		&&(!done))
		{
			final MOB mob=(MOB)ticking;
			final Ability pregA=mob.fetchEffect("Pregnancy");
			if(pregA != null)
			{
				done=true;
				if(chance < 0)
					chance = 50+(5*super.adjustedLevel(invoker(), 0))+(10*super.getXLEVELLevel(invoker()));
				while(chance > 100)
				{
					final int numKids = CMath.s_int(pregA.getStat("NUMBABIES"));
					pregA.setStat("NUMBABIES", ""+(numKids+1));
					chance -=100;
				}
				if(CMLib.dice().rollPercentage()<chance)
				{
					final int numKids = CMath.s_int(pregA.getStat("NUMBABIES"));
					pregA.setStat("NUMBABIES", ""+(numKids+1));
				}
				if(canBeUninvoked())
					unInvoke();
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.charStats().reproductiveCode()!='F')
		{
			mob.tell(L("@x1 seems unlike to have a womb.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto) && (auto||(target.fetchEffect("Pregnancy")==null));
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> seem(s) more bountiful!"));
				final Chant_BountifulWomb wA = (Chant_BountifulWomb)beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
				if(wA != null)
					wA.chance = 50+(5*super.adjustedLevel(mob, 0))+(10*super.getXLEVELLevel(mob));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
