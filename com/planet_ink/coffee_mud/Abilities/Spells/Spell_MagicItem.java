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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Spell_MagicItem extends Spell
{
	@Override public String ID() { return "Spell_MagicItem"; }
	@Override public String name(){return "Magic Item";}
	@Override protected int canTargetCode(){return CAN_ITEMS;}
	@Override public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	@Override public long flags(){return Ability.FLAG_NOORDERING;}
	protected int overridemana(){return Ability.COST_ALL;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(_("Enchant which spell onto what?"));
			return false;
		}
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(_("You don't see '@x1' here.",((String)commands.lastElement())));
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell(mob,target,null,_("You can't enchant <T-NAME>."));
			return false;
		}

		commands.removeElementAt(commands.size()-1);
		final Item wand=(Item)target;

		final String spellName=CMParms.combine(commands,0).trim();
		Spell wandThis=null;
		final Vector ables=new Vector();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(A instanceof Spell)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(!A.ID().equals(this.ID())))
				ables.addElement(A);
		}
		wandThis = (Spell)CMLib.english().fetchEnvironmental(ables,spellName,true);
		if(wandThis==null)
			wandThis = (Spell)CMLib.english().fetchEnvironmental(ables,spellName,false);
		if(wandThis==null)
		{
			mob.tell(_("You don't know how to enchant anything with '@x1'.",spellName));
			return false;
		}


		if((wandThis.ID().equals("Spell_Stoneskin"))
		||(wandThis.ID().equals("Spell_MirrorImage"))
		||(CMath.bset(wandThis.flags(), FLAG_SUMMONING)))
		{
			mob.tell(_("That spell cannot be used to enchant anything."));
			return false;
		}

		if((CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID())>24)
		||(((StdAbility)wandThis).usageCost(null,true)[0]>45))
		{
			mob.tell(_("That spell is too powerful to enchant into anything."));
			return false;
		}

		if((wand.numEffects()>0)||(!wand.isGeneric()))
		{
			mob.tell(_("You can't enchant '@x1'.",wand.name()));
			return false;
		}

		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID()));
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(_("You don't have enough experience to cast this spell."));
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			mob.tell(_("You lose @x1 experience points for the effort.",""+experienceToLose));
			setMiscText(wandThis.ID());
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),_("^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,_("<T-NAME> glow(s) brightly!"));
				wand.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_BONUS);
				wand.basePhyStats().setLevel(wand.basePhyStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID())/2));
				//Vector V=CMParms.parseCommas(CMLib.utensils().wornList(wand.rawProperLocationBitmap()),true);
				if(wand instanceof Armor)
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if(wand instanceof Weapon)
				{
					final Ability A=CMClass.getAbility("Prop_FightSpellCast");
					A.setMiscText("25%;MAXTICKS=12;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if((wand instanceof Food)
				||(wand instanceof Drink))
				{
					final Ability A=CMClass.getAbility("Prop_UseSpellCast2");
					A.setMiscText(wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if(wand.fitsOn(Wearable.WORN_HELD)||wand.fitsOn(Wearable.WORN_WIELD))
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				{
					final Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				wand.recoverPhyStats();
			}

		}
		else
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			mob.tell(_("You lose @x1 experience points for the effort.",""+experienceToLose));
			beneficialWordsFizzle(mob,target,_("<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly, and looking very frustrated."));
		}


		// return whether it worked
		return success;
	}
}
