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
public class Spell_ChangeSex extends Spell
{
	public String ID() { return "Spell_ChangeSex"; }
	public String name(){return "Change Sex";}
	public String displayText(){return "(Change Sex)";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_TRANSMUTATION;}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		char gender='M';
		if(affectableStats.getStat(CharStats.STAT_GENDER)!='F')
			gender='F';
		affectableStats.setStat(CharStats.STAT_GENDER,gender);
	}

	public void setChildStuff(MOB M, Environmental target)
	{
		if(CMLib.flags().isChild(M))
		{
			if(M.charStats().getStat(CharStats.STAT_GENDER)=='F')
			{
				M.setDescription(CMStrings.replaceAll(M.description()," son "," daughter "));
				if(target!=null)
					target.setDescription(CMStrings.replaceAll(target.description()," son "," daughter "));
			}
			else
			{
				M.setDescription(CMStrings.replaceAll(M.description()," daughter "," son "));
				if(target!=null)
					target.setDescription(CMStrings.replaceAll(target.description()," daughter "," son "));
			}
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected instanceof CagedAnimal)
		{
			CagedAnimal target=(CagedAnimal)affected;
			MOB mob=target.unCageMe();
			super.unInvoke();
			if(canBeUninvoked())
			{
				Ability A=mob.fetchEffect(ID());
				if(A!=null) mob.delEffect(A);
				mob.recoverCharStats();
				mob.recoverEnvStats();
				setChildStuff(mob, target);
				Room R=CMLib.map().roomLocation(target);
				if(R!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) like <S-HIS-HER> old self again.");
				
			}
			
		}
		else
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			super.unInvoke();
			if(canBeUninvoked())
				if((mob.location()!=null)&&(!mob.amDead()))
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) like <S-HIS-HER> old self again.");
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=super.getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		if(target instanceof Item)
		{
			if(!(target instanceof CagedAnimal))
			{
				mob.tell("This spell won't have much effect on "+target.name()+".");
				return false;
			}
		}
		else
		if(!(target instanceof MOB))
		{
			mob.tell("This spell won't have much effect on "+target.name()+".");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> sing(s) a spell to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					MOB M=null;
					if(target instanceof MOB)
					{
						success=beneficialAffect(mob,target,asLevel,0);
						M=(MOB)target;
						M.recoverCharStats();
						M.recoverEnvStats();
					}
					else
					if(target instanceof CagedAnimal)
					{
						M=((CagedAnimal)target).unCageMe();
						char gender='M';
						if(M.baseCharStats().getStat(CharStats.STAT_GENDER)!='F')
							gender='F';
						M.baseCharStats().setStat(CharStats.STAT_GENDER,gender);
						M.recoverCharStats();
						M.recoverEnvStats();
						setChildStuff(M, target);
						M.text();
						((CagedAnimal)target).cageMe(M);
						target.text();
					}
					else
						return false;
					M.recoverCharStats();
					target.recoverEnvStats();
					mob.location().show(M,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) "+M.charStats().genderName()+"!");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> sing(s) a spell to <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
