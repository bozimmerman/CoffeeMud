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
public class Spell_EnchantArrows extends Spell
{
	public String ID() { return "Spell_EnchantArrows"; }
	public String name(){return "Enchant Arrows";}
	protected int canTargetCode(){return CAN_ITEMS;}
	protected int canAffectCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_NOORDERING;}

	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
        affectableStats.setAbility(affectableStats.ability()+Util.s_int(text()));
        affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=super.getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((!(target instanceof Ammunition))||(!((Ammunition)target).ammunitionType().equalsIgnoreCase("arrows")))
		{
			mob.tell("You can't enchant that with an Enchant Arrows spell!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		MUDFight.postExperience(mob,null,null,-5,false);

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect(ID());
				if((A!=null)&&(Util.s_int(A.text())>2))
					mob.tell("You are not able to enchant "+target.name()+" further.");
				else
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> glows!");
					if(A==null){ A=(Ability)copyOf(); target.addNonUninvokableEffect(A);}
					A.setMiscText(""+(Util.s_int(A.text())+1));
					target.recoverEnvStats();
					mob.recoverEnvStats();
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> tightly and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}
