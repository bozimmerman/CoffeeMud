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
public class Spell_Polymorph extends Spell
{
	public String ID() { return "Spell_Polymorph"; }
	public String name(){return "Polymorph";}
	public String displayText(){return "(Polymorph)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	private Race newRace=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setName("a "+newRace.name()+" called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the "+newRace.name());
			int oldAdd=affectableStats.weight()-affected.baseEnvStats().weight();
			newRace.setHeightWeight(affectableStats,(char)((MOB)affected).charStats().getStat(CharStats.GENDER));
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);
		}
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
			affectableStats.setMyRace(newRace);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> morph(s) back into <S-HIM-HERSELF> again.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,-(target.charStats().getStat(CharStats.CONSTITUTION)*2),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> form(s) a spell around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					newRace=null;
					while((newRace==null)||(newRace.ID().equals("StdRace"))||(newRace.availability()==Race.AVAILABLE_NONE))
						newRace=CMClass.randomRace();
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) a "+newRace.name()+"!");
					success=beneficialAffect(mob,target,asLevel,0);
					target.recoverCharStats();
					target.confirmWearability();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> form(s) a spell around <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
