package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_Shamblermorph extends Chant
{
	public String ID() { return "Chant_Shamblermorph"; }
	public String name(){ return "Shamblermorph";}
	public String displayText(){return "(Shamblermorph)";}
	public int quality(){return Ability.OK_OTHERS;}
	protected static Race treeForm=null;

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(treeForm==null) treeForm=CMClass.getRace("Shambler");
		if(treeForm!=null)	affectableStats.setMyRace(treeForm);
		affectableStats.setStat(CharStats.GENDER,'N');
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		if((treeForm!=null)&&(affected instanceof MOB))
		{
			if(affected.name().indexOf(" ")>0)
				affectableStats.setName("a shambling mound called "+affected.name());
			else
				affectableStats.setName(affected.name()+" the shambling mound");
			treeForm.setHeightWeight(((MOB)affected).baseEnvStats(),'M');
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer a shambling mound.");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-(levelDiff*10),auto);
		boolean malicious=!target.getGroupMembers(new HashSet()).contains(mob);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,(malicious?CMMsg.MASK_MALICIOUS:0)|affectType(auto),auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"Leaves sprout from <S-YOUPOSS> skin as <S-HE-SHE> grow(s) into a Shambling Mound!");
					if(malicious)
						maliciousAffect(mob,target,0,-1);
					else
						beneficialAffect(mob,target,0);
				}
			}
		}
		else
		if(malicious)
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");

		// return whether it worked
		return success;
	}
}
