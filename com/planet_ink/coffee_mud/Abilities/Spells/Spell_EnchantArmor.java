package com.planet_ink.coffee_mud.Abilities.Spells;
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
@SuppressWarnings("rawtypes")
public class Spell_EnchantArmor extends Spell
{
	@Override public String ID() { return "Spell_EnchantArmor"; }
	@Override public String name(){return "Enchant Armor";}
	@Override protected int canTargetCode(){return CAN_ITEMS;}
	@Override public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	@Override public long flags(){return Ability.FLAG_NOORDERING;}
	protected int overridemana(){return Ability.COST_ALL;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!(target instanceof Armor))
		{
			mob.tell(mob,target,null,"You can't enchant <T-NAME> with an Enchant Armor spell!");
			return false;
		}
		if(target.phyStats().ability()>2)
		{
			mob.tell(target.name(mob)+" cannot be enchanted further.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int experienceToLose=getXPCOSTAdjustment(mob,50);
		CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
		mob.tell("The effort causes you to lose "+experienceToLose+" experience.");

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> glows!");
				target.basePhyStats().setAbility(target.basePhyStats().ability()+1);
				target.basePhyStats().setLevel(target.basePhyStats().level()+3);
				target.recoverPhyStats();
				mob.recoverPhyStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> tightly and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}
