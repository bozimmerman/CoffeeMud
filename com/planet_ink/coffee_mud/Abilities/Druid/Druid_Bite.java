package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Druid_Bite extends StdAbility
{
	public String ID() { return "Druid_Bite"; }
	public String name(){ return "Bite";}
	private static final String[] triggerStrings = {"BITE"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to bite!");
			return false;
		}
		if(!Druid_ShapeShift.isShapeShifted(mob))
		{
			mob.tell("You must be in your animal form to bite.");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_MOUTH)<=0)
		{
			mob.tell("You must have a mouth to bite!");
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
		boolean success=profficiencyCheck(mob,mob.charStats().getStat(CharStats.STRENGTH)-target.charStats().getStat(CharStats.STRENGTH)-10,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			int topDamage=adjustedLevel(mob,asLevel)+2;
			int damage=Dice.roll(1,topDamage,0);
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()>0)
					damage = (int)Math.round(Util.div(damage,2.0));
				MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISYMOVEMENT,Weapon.TYPE_PIERCING,"^F<S-NAME> <DAMAGE> <T-NAME> with a piercing BITE!^?");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to bite <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
