package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String ID() { return "Spell_EnchantWand"; }
	public String name(){return "Enchant Wand";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public long flags(){return Ability.FLAG_NOORDERING;}
	protected int overrideMana(){return Integer.MAX_VALUE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Enchant which spell onto what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		if(!(target instanceof Wand))
		{
			mob.tell("You can't enchant that.");
			return false;
		}

		commands.removeElementAt(commands.size()-1);
		Wand wand=(Wand)target;

		String spellName=Util.combine(commands,0).trim();
		Spell wandThis=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A instanceof Spell)
			&&(A.isBorrowed(mob)||(CMAble.qualifiesByLevel(mob,A)))
			&&(A.name().toUpperCase().startsWith(spellName.toUpperCase()))
			&&(!A.ID().equals(this.ID())))
				wandThis=(Spell)A;
		}
		if(wandThis==null)
		{
			mob.tell("You don't know how to enchant anything with '"+spellName+"'.");
			return false;
		}

		if(wand.getSpell()!=null)
		{
			mob.tell("A spell has already been enchanted into '"+wand.name()+"'.");
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int experienceToLose=10*CMAble.lowestQualifyingLevel(wandThis.ID());
		MUDFight.postExperience(mob,null,null,-experienceToLose,false);
		mob.tell("You lose "+experienceToLose+" experience points for the effort.");

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			setMiscText(wandThis.ID());
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				wand.setSpell((Ability)wandThis.copyOf());
				if((wand.usesRemaining()==Integer.MAX_VALUE)||(wand.usesRemaining()<0))
					wand.setUsesRemaining(0);
				wand.baseEnvStats().setLevel(CMAble.lowestQualifyingLevel(wandThis.ID())+2);
				wand.setUsesRemaining(wand.usesRemaining()+5);
				wand.text();
				wand.recoverEnvStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
