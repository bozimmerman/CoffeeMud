package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Chant_EnchantShards extends Chant
{

	@Override
	public String ID()
	{
		return "Chant_EnchantShards";
	}

	private final static String localizedName = CMLib.lang().L("Enchant Shards");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING;
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Enchant which chant onto what?"));
			return false;
		}
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(commands.size()-1))));
			return false;
		}
		if(!(target instanceof Wand))
		{
			mob.tell(mob,target,null,L("You can't enchant <T-NAME>."));
			return false;
		}
		if((((Wand)target).getEnchantType()!=-1)
		&&(((Wand)target).getEnchantType()!=Ability.ACODE_CHANT))
		{
			mob.tell(mob,target,null,L("You can't enchant <T-NAME> with this chant."));
			return false;
		}

		commands.remove(commands.size()-1);
		final Wand wand=(Wand)target;

		final String spellName=CMParms.combine(commands,0).trim();
		Ability wandThis=null;
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(A.name().equalsIgnoreCase(spellName))
			&&(!A.ID().equals(this.ID())))
				wandThis=A;
		}
		if(wandThis==null)
		{
			for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
				&&(CMLib.english().containsString(A.name(),spellName))
				&&(!A.ID().equals(this.ID())))
					wandThis=A;
			}
		}
		if(wandThis==null)
		{
			mob.tell(L("You don't know how to enchant anything with '@x1'.",spellName));
			return false;
		}

		if((CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID())>24)
		||(((StdAbility)wandThis).usageCost(null,true)[0]>45)
		||(CMath.bset(wandThis.flags(), Ability.FLAG_CLANMAGIC)))
		{
			mob.tell(L("That spell is too powerful to enchant into anything."));
			return false;
		}

		if(wand.getSpell()!=null)
		{
			mob.tell(L("A chant has already been enchanted into '@x1'.",wand.name()));
			return false;
		}

		int experienceToLose=10*CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID());
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to do that."));
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
		experienceToLose=-CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
		mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			setMiscText(wandThis.ID());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"<T-NAME> appear(s) enchanted!":"^S<S-NAME> enchant(s) <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				wand.setSpell((Ability)wandThis.copyOf());
				if((wand.usesRemaining()==Integer.MAX_VALUE)||(wand.usesRemaining()<0))
					wand.setUsesRemaining(0);
				final int newLevel=wandThis.adjustedLevel(mob, asLevel);
				if(newLevel > wand.basePhyStats().level())
					wand.basePhyStats().setLevel(newLevel);
				wand.setUsesRemaining(wand.usesRemaining()+5);
				wand.text();
				wand.recoverPhyStats();
			}

		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) for enchantments, but nothing happens."));

		// return whether it worked
		return success;
	}
}
