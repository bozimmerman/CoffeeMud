package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spell_MagicItem extends Spell
{
	public String ID() { return "Spell_MagicItem"; }
	public String name(){return "Magic Item";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public long flags(){return Ability.FLAG_NOORDERING;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("Enchant which spell onto what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell(mob,target,null,"You can't enchant <T-NAME>.");
			return false;
		}

		commands.removeElementAt(commands.size()-1);
		Item wand=(Item)target;

		String spellName=CMParms.combine(commands,0).trim();
		Spell wandThis=null;
		Vector ables=new Vector();
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A instanceof Spell)
			&&((!A.savable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(!A.ID().equals(this.ID())))
				ables.addElement(A);
		}
		wandThis = (Spell)CMLib.english().fetchEnvironmental(ables,spellName,true);
		if(wandThis==null)
			wandThis = (Spell)CMLib.english().fetchEnvironmental(ables,spellName,false);
		if(wandThis==null)
		{
			mob.tell("You don't know how to enchant anything with '"+spellName+"'.");
			return false;
		}

		if((wandThis.ID().equals("Spell_Stoneskin"))
		||(wandThis.ID().equals("Spell_MirrorImage"))
		||(CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID())>24)
		||(((StdAbility)wandThis).usageCost(null,true)[0]>45))
		{
			mob.tell("That spell is too powerful to enchant into anything.");
			return false;
		}

		if((wand.numEffects()>0)||(!wand.isGeneric()))
		{
			mob.tell("You can't enchant '"+wand.name()+"'.");
			return false;
		}

		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID()));
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell("You don't have enough experience to cast this spell.");
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
	        experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			mob.tell("You lose "+experienceToLose+" experience points for the effort.");
			setMiscText(wandThis.ID());
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,"<T-NAME> glow(s) brightly!");
				wand.baseEnvStats().setDisposition(target.baseEnvStats().disposition()|EnvStats.IS_BONUS);
				wand.baseEnvStats().setLevel(wand.baseEnvStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(wandThis.ID())/2));
				//Vector V=CMParms.parseCommas(CMLib.utensils().wornList(wand.rawProperLocationBitmap()),true);
				if(wand instanceof Armor)
				{
					Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if(wand instanceof Weapon)
				{
					Ability A=CMClass.getAbility("Prop_FightSpellCast");
					A.setMiscText("25%;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if((wand instanceof Food)
				||(wand instanceof Drink))
				{
					Ability A=CMClass.getAbility("Prop_UseSpellCast2");
					A.setMiscText(wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				if(wand.fitsOn(Wearable.WORN_HELD)||wand.fitsOn(Wearable.WORN_WIELD))
				{
					Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				else
				{
					Ability A=CMClass.getAbility("Prop_WearSpellCast");
					A.setMiscText("LAYERED;"+wandThis.ID()+";");
					wand.addNonUninvokableEffect(A);
				}
				wand.recoverEnvStats();
			}

		}
		else
		{
	        experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			mob.tell("You lose "+experienceToLose+" experience points for the effort.");
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly, and looking very frustrated.");
		}


		// return whether it worked
		return success;
	}
}
