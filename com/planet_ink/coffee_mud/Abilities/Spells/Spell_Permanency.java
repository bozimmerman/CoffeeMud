package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_Permanency extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Permanency";
	}

	private final static String localizedName = CMLib.lang().L("Permanency");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS|CAN_MOBS|CAN_EXITS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS|CAN_MOBS|CAN_EXITS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(((mob.baseState().getMana()<100)||(mob.maxState().getMana()<100))||(mob.isMonster()))
		{
			mob.tell(L("You aren't powerful enough to cast this."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> incant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StdAbility theOne=null;
				for(int a=target.numEffects()-1;a>=0;a--) // personal effects
				{
					final Ability A=target.fetchEffect(a);
					if((A.invoker()==mob)
					 &&(!A.isAutoInvoked())
					 &&(A.canBeUninvoked())
					 &&(A instanceof StdAbility)
					 &&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
					{
						theOne=(StdAbility)A;
						break;
					}
				}
				if(theOne==null)
				{
					mob.tell(L("There does not appear to be any of your spells on @x1 which can be made permanent.",target.name(mob)));
					return false;
				}
				else
				if(((target instanceof Room)||(target instanceof Exit))
				&&(theOne.enchantQuality()==Ability.QUALITY_MALICIOUS)
				&&(!CMLib.law().doesOwnThisLand(mob,mob.location())))
				{
					mob.tell(L("You can not make @x1 permanent here.",theOne.name()));
					return false;
				}
				else
				{
					theOne.makeNonUninvokable();
					theOne.setSavable(true);
					mob.baseState().setMana(mob.baseState().getMana()-100);
					mob.maxState().setMana(mob.maxState().getMana()-100);
					target.text();
					if((target instanceof Room)
					&&(CMLib.law().doesOwnThisLand(mob,(Room)target)))
						CMLib.database().DBUpdateRoom((Room)target);
					else
					if(target instanceof Exit)
					{
						final Room R=mob.location();
						Room R2=null;
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							if(R.getExitInDir(d)==target)
							{
								R2=R.getRoomInDir(d);
								break;
							}
						}
						if((CMLib.law().doesOwnThisLand(mob,R))
						||((R2!=null)&&(CMLib.law().doesOwnThisLand(mob,R2))))
							CMLib.database().DBUpdateExits(R);
					}
					mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("The quality of @x1 inside <T-NAME> glows!",theOne.name()));
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> incant(s) to <T-NAMESELF>, but lose(s) patience."));

		// return whether it worked
		return success;
	}
}
