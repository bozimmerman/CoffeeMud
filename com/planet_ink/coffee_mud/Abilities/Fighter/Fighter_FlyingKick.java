package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Fighter_FlyingKick extends StdAbility
{
	public String ID() { return "Fighter_FlyingKick"; }
	public String name(){ return "Flying Kick";}
	private static final String[] triggerStrings = {"FLYINGKICK","FLYKICK"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	public int minRange(){return 1;}
	public int maxRange(){return 5;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if((student.fetchAbility("Fighter_AxKick")==null))
		{
			teacher.tell(student.name()+" has not yet learned the Ax Kick skill.");
			student.tell("You need to learn the Ax Kick skill to learn "+name()+".");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()==0))
		{
			mob.tell("You are too close to do a flying kick!");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_LEG)<=1)
		{
			mob.tell("You need at least two legs to do this.");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,(mob.charStats().getStat(CharStats.DEXTERITY)-target.charStats().getStat(CharStats.DEXTERITY))*2,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			int topDamage=adjustedLevel(mob,asLevel)+20;
			int damage=Dice.roll(1,topDamage,0);
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()>0)
					damage = (int)Math.round(Util.div(damage,2.0));
				MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISYMOVEMENT,Weapon.TYPE_BASHING,"^F^<FIGHT^><S-NAME> <DAMAGE> <T-NAME> with a flying KICK!^</FIGHT^>^?"+CommonStrings.msp("bashed1.wav",30));
				if(mob.getVictim()==target)
				{
					mob.setAtRange(0);
					target.setAtRange(0);
				}
				if(mob.getVictim()==null) mob.setVictim(null); // correct range
				if(target.getVictim()==null) target.setVictim(null); // correct range
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to land the flying kick on  <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
