package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2003-2025 Bo Zimmerman

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
public class Spell_EnchantWand extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_EnchantWand";
	}

	private final static String localizedName = CMLib.lang().L("Enchant Wand");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;
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
			mob.tell(L("Enchant which spell onto what?"));
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
		&&(((Wand)target).getEnchantType()!=Ability.ACODE_SPELL))
		{
			mob.tell(mob,target,null,L("You can't enchant <T-NAME> with this spell."));
			return false;
		}

		commands.remove(commands.size()-1);
		final Wand wand=(Wand)target;

		final String spellName=CMParms.combine(commands,0).trim();
		Ability wandThisA=null;
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(A.name().equalsIgnoreCase(spellName))
			&&(!A.ID().equals(this.ID())))
				wandThisA=A;
		}
		if(wandThisA==null)
		{
			for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
				&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
				&&(CMLib.english().containsString(A.name(),spellName))
				&&(!A.ID().equals(this.ID())))
					wandThisA=A;
			}
		}
		if(wandThisA==null)
		{
			mob.tell(L("You don't know how to enchant anything with '@x1'.",spellName));
			return false;
		}

		if(!wandThisA.mayBeEnchanted())
		{
			mob.tell(L("That spell is too powerful to enchant into wands."));
			return false;
		}

		if(wand.getSpell()!=null)
		{
			mob.tell(L("A spell has already been enchanted into '@x1'.",wand.name()));
			return false;
		}

		int experienceToLose=10*CMLib.ableMapper().lowestQualifyingLevel(wandThisA.ID());
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to cast this spell."));
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
		experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
		mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			setMiscText(wandThisA.ID());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				wand.setSpell((Ability)wandThisA.copyOf());
				if((wand.usesRemaining()==Integer.MAX_VALUE)||(wand.usesRemaining()<0))
					wand.setUsesRemaining(0);
				final int lowestSpellLevel = CMLib.ableMapper().lowestQualifyingLevel(wandThisA.ID());
				if(lowestSpellLevel > wand.basePhyStats().level())
					wand.basePhyStats().setLevel(lowestSpellLevel);
				wand.setUsesRemaining(wand.usesRemaining()+5);
				wand.text();
				wand.recoverPhyStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly, and looking very frustrated."));

		// return whether it worked
		return success;
	}
}
