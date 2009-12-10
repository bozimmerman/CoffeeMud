package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Contagion extends Prayer implements DiseaseAffect
{
	public String ID() { return "Prayer_Contagion"; }
	public String displayText(){ return "(Contagion)";}
	public String name(){ return "Contagion";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int difficultyLevel(){return 0;}

	public void unInvoke()
	{
		if(affected==null) return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			    spreadImmunity(mob);
			mob.tell("The contagion fades.");
		}
		super.unInvoke();
	}

	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(affected==null) return false;
		if(!(affected instanceof MOB)) return false;
		MOB mob=(MOB)affected;
		if(mob.location().numInhabitants()==1)
			return true;
		Vector choices=new Vector();
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)
			   &&(A.canBeUninvoked())
			   &&(!A.ID().equals(ID()))
			   &&(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
			   &&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
				  ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
			   &&(!A.isAutoInvoked()))
				choices.addElement(A);
		}
		if(choices.size()==0) return true;
		MOB target=mob.location().fetchInhabitant(CMLib.dice().roll(1,mob.location().numInhabitants(),-1));
		Ability thisOne=(Ability)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
		if((target==null)||(thisOne==null)||(target.fetchEffect(ID())!=null))
			return true;
		if(CMLib.dice().rollPercentage()>(target.charStats().getSave(CharStats.STAT_SAVE_DISEASE)))
		{
			((Ability)this.copyOf()).invoke(target,target,true,0);
			if(target.fetchEffect(ID())!=null)
				((Ability)thisOne.copyOf()).invoke(target,target,true,0);
		}
		else
		    spreadImmunity(target);
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"<T-NAME> become(s) contagious!":"^S<S-NAME> "+prayWord(mob)+" for a contagion to inflict <T-NAMESELF>.^?");
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.TYP_DISEASE|CMMsg.MASK_MALICIOUS,null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
					success=maliciousAffect(mob,target,asLevel,0,-1);
				else
				    spreadImmunity(target);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
