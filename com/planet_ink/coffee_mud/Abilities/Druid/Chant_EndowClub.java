package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_EndowClub extends Chant
{

	@Override
	public String ID()
	{
		return "Chant_EndowClub";
	}

	private final static String	localizedName	= CMLib.lang().L("Endow Club");

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
		return Ability.ACODE_CHANT | Ability.DOMAIN_DEEPMAGIC;
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
			mob.tell(L("Endow which chant onto which club?"));
			return false;
		}
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(commands.size()-1))));
			return false;
		}
		if((!(target instanceof Weapon))
		||(((Weapon)target).weaponClassification()!=Weapon.CLASS_BLUNT)
		||(((((Weapon)target).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
			&&((((Weapon)target).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_VEGETATION)))
		{
			mob.tell(mob,target,null,L("You can't endow <T-NAME>."));
			return false;
		}

		commands.remove(commands.size()-1);
		final Item clubI=(Item)target;

		final String spellName=CMParms.combine(commands,0).trim();
		Ability enchantSpellA=null;
		final Vector<Ability> ables=new Vector<Ability>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(!A.ID().equals(this.ID())))
				ables.addElement(A);
		}
		enchantSpellA = (Ability)CMLib.english().fetchEnvironmental(ables,spellName,true);
		if(enchantSpellA==null)
			enchantSpellA = (Ability)CMLib.english().fetchEnvironmental(ables,spellName,false);
		if(enchantSpellA==null)
		{
			mob.tell(L("You don't know how to endow anything with '@x1'.",spellName));
			return false;
		}

		if((CMath.bset(enchantSpellA.flags(), FLAG_SUMMONING))
		||(enchantSpellA.canAffect(CAN_ROOMS)))
		{
			mob.tell(L("That chant cannot be used to endow anything."));
			return false;
		}

		if(!enchantSpellA.mayBeEnchanted())
		{
			mob.tell(L("That chant is too powerful to endow into anything."));
			return false;
		}

		if((clubI.numEffects()>0)||(!clubI.isGeneric()))
		{
			mob.tell(L("You can't endow '@x1'.",clubI.name()));
			return false;
		}

		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(enchantSpellA.ID()));
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to endow this chant."));
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			setMiscText(enchantSpellA.ID());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) brightly!"));
				clubI.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_BONUS);
				clubI.basePhyStats().setLevel(clubI.basePhyStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(enchantSpellA.ID())/2));
				//Vector<String> V=CMParms.parseCommas(CMLib.utensils().wornList(wand.rawProperLocationBitmap()),true);
				final Ability A=CMClass.getAbility("Prop_FightSpellCast");
				A.setMiscText("25%;MAXTICKS=12;"+enchantSpellA.ID()+";");
				clubI.addNonUninvokableEffect(A);
				clubI.recoverPhyStats();
			}

		}
		else
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));
		}
		// return whether it worked
		return success;
	}
}
