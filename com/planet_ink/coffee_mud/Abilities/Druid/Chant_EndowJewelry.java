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
   Copyright 2024-2025 Bo Zimmerman

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
public class Chant_EndowJewelry extends Chant
{

	@Override
	public String ID()
	{
		return "Chant_EndowJewelry";
	}

	private final static String	localizedName	= CMLib.lang().L("Endow Jewelry");

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
			mob.tell(L("Endow which chant onto what jewelry?"));
			return false;
		}
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",(commands.get(commands.size()-1))));
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell(mob,target,null,L("You can't endow <T-NAME>."));
			return false;
		}
		final long goodCheck = ((Armor)target).rawProperLocationBitmap()
				& ( Wearable.WORN_EARS | Wearable.WORN_RIGHT_FINGER | Wearable.WORN_LEFT_FINGER
						| Wearable.WORN_NECK | Wearable.WORN_LEFT_WRIST | Wearable.WORN_RIGHT_WRIST);
		if(goodCheck == 0)
		{
			mob.tell(L("@x1 can not be endowed with this magic, as it is not worn on the ears, fingers, neck, or wrist.",target.name(mob)));
			return false;
		}
		final long badCheck = ((Armor)target).rawProperLocationBitmap()
				& ( Wearable.WORN_TORSO | Wearable.WORN_ARMS | Wearable.WORN_FEET | Wearable.WORN_ABOUT_BODY | Wearable.WORN_HANDS | Wearable.WORN_HEAD);
		if(badCheck != 0)
		{
			mob.tell(L("@x1 can not be endowed with this magic, as it is not worn exclusively on the ears, fingers, neck, or wrist.",target.name(mob)));
			return false;
		}

		commands.remove(commands.size()-1);
		final Item targetI=(Item)target;

		final String chantName=CMParms.combine(commands,0).trim();
		Ability endowChantA=null;
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
		endowChantA = (Ability)CMLib.english().fetchEnvironmental(ables,chantName,true);
		if(endowChantA==null)
			endowChantA = (Ability)CMLib.english().fetchEnvironmental(ables,chantName,false);
		if(endowChantA==null)
		{
			mob.tell(L("You don't know how to endow anything with '@x1'.",chantName));
			return false;
		}

		if((CMath.bset(endowChantA.flags(), FLAG_SUMMONING))
		||(endowChantA.canAffect(CAN_ROOMS)))
		{
			mob.tell(L("That chant cannot be used to endow anything."));
			return false;
		}

		if(!endowChantA.mayBeEnchanted())
		{
			mob.tell(L("That chant is too powerful to endow into anything."));
			return false;
		}

		if((targetI.numEffects()>0)||(!targetI.isGeneric()))
		{
			mob.tell(L("You can't endow '@x1'.",targetI.name()));
			return false;
		}

		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(endowChantA.ID()));
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to use this magic."));
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
			setMiscText(endowChantA.ID());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) brightly!"));
				targetI.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_BONUS);
				targetI.basePhyStats().setLevel(targetI.basePhyStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(endowChantA.ID())/2));
				//Vector<String> V=CMParms.parseCommas(CMLib.utensils().wornList(wand.rawProperLocationBitmap()),true);
				if(targetI instanceof Armor)
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+endowChantA.ID()+";");
					targetI.addNonUninvokableEffect(A);
				}
				if(targetI.fitsOn(Wearable.WORN_HELD)||targetI.fitsOn(Wearable.WORN_WIELD))
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+endowChantA.ID()+";");
					targetI.addNonUninvokableEffect(A);
				}
				else
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+endowChantA.ID()+";");
					targetI.addNonUninvokableEffect(A);
				}
				targetI.recoverPhyStats();
			}

		}
		else
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, looking very frustrated."));
		}
		// return whether it worked
		return success;
	}
}
