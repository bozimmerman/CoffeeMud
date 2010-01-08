package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_EnchantArrows extends Spell
{
	public String ID() { return "Spell_EnchantArrows"; }
	public String name(){return "Enchant Arrows";}
	protected int canTargetCode(){return CAN_ITEMS;}
	protected int canAffectCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_NOORDERING;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
        affectableStats.setAbility(affectableStats.ability()+CMath.s_int(text()));
        affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=super.getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if((!(target instanceof Ammunition))||(!((Ammunition)target).ammunitionType().equalsIgnoreCase("arrows")))
		{
			mob.tell(mob,target,null,"You can't enchant <T-NAME> ith an Enchant Arrows spell!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

        int experienceToLose=getXPCOSTAdjustment(mob,5);
		CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=target.fetchEffect(ID());
				if((A!=null)&&(CMath.s_int(A.text())>2))
					mob.tell("You are not able to enchant "+target.name()+" further.");
				else
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> glows!");
					if(A==null){ A=(Ability)copyOf(); target.addNonUninvokableEffect(A);}
					A.setMiscText(""+(CMath.s_int(A.text())+1));
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
