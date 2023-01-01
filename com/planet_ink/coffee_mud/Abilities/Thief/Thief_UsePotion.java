package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2020-2023 Bo Zimmerman

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
public class Thief_UsePotion extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_UsePotion";
	}

	private final static String localizedName = CMLib.lang().L("Use Potion");

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
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"POTION","USEPOTION"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_POISONING;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public List<Ability> returnOffensiveAffects(final Potion fromMe)
	{
		final List<Ability> offenders=new ArrayList<Ability>();

		for(final Iterator<Ability> a=fromMe.getSpells().iterator();a.hasNext();)
		{
			final Ability A=a.next();
			if((A!=null)
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
			&&(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
			&&(A.canTarget(Ability.CAN_MOBS))
			&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())<24)
			&&(!A.ID().equals("Poison_Rotten")))
				offenders.add(A);
		}
		return offenders;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("What would you like to apply a potion to, and which potion would you use?"));
			return false;
		}
		final Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,commands.get(0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(0))));
			return false;
		}
		if((!(target instanceof Food))
		&&(!(target instanceof Drink))
		&&(!(target instanceof Weapon)))
		{
			mob.tell(L("You don't know how to apply a potion to @x1.",target.name(mob)));
			return false;
		}
		if(target.numEffects()>0)
		{
			if(target.fetchEffect("TemporaryAffects")!=null)
			{
				mob.tell(L("@x1 is already affected by something.",target.name(mob)));
				return false;
			}
			if((!(target instanceof Weapon))
			&&target.fetchEffect("Prop_UseSpellCast2")!=null)
			{
				mob.tell(L("@x1 is already affected by something.",target.name(mob)));
				return false;
			}
		}
		final Item potion=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,CMParms.combine(commands,1));
		if((potion==null)||(!CMLib.flags().canBeSeenBy(potion,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",CMParms.combine(commands,1)));
			return false;
		}
		if(!(potion instanceof Potion))
		{
			mob.tell(L("@x1 is not a potion!",potion.name()));
			return false;
		}
		final List<Ability> V=returnOffensiveAffects((Potion)potion);
		if((V.size()==0)||(!(potion instanceof Drink)))
		{
			if(potion.fetchEffect("Poison_Rotten")!=null)
				mob.tell(L("@x1 is no longer a potion!",potion.name()));
			else
				mob.tell(L("@x1 is not an appropriate potion!",potion.name()));
			return false;
		}
		final Drink dPotion=(Drink)potion;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,L("<S-NAME> attempt(s) to apply a potion to <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				final Ability A=V.get(0);
				if(A!=null)
				{
					if(target instanceof Weapon)
					{
						Ability tempA=target.fetchEffect("TemporaryAffects");
						if(tempA == null)
						{
							tempA=CMClass.getAbility("TemporaryAffects");
							tempA.startTickDown(mob, target, 10);
							tempA.makeLongLasting();
						}
						tempA.setMiscText("+Prop_FightSpellCast "+adjustedLevel(mob,asLevel)+" 30% "+A.ID());
					}
					else
					{
						final Ability tempA=CMClass.getAbility("Prop_UseSpellCast2");
						target.addNonUninvokableEffect(tempA);
						tempA.setMiscText(A.ID());
					}

					int amountToTake=dPotion.thirstQuenched()/5;
					if(amountToTake<1)
						amountToTake=1;
					dPotion.setLiquidRemaining(dPotion.liquidRemaining()-amountToTake);
					if(dPotion.disappearsAfterDrinking()
					||((dPotion instanceof RawMaterial)&&(dPotion.liquidRemaining()<=0)))
						dPotion.destroy();
				}

			}
		}
		return success;
	}

}
