package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Druid_Rend extends StdAbility
{
	public String ID() { return "Druid_Rend"; }
	public String name(){ return "Rend";}
	private static final String[] triggerStrings = {"REND"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;}
	public int usageType(){return USAGE_MOVEMENT;}

    public int castingQuality(MOB mob, Environmental target)
    {
         if(mob!=null)
         {
            if(mob.isInCombat()&&(mob.rangeToTarget()>0))
                 return Ability.QUALITY_INDIFFERENT;
            if(!Druid_ShapeShift.isShapeShifted(mob))
                return Ability.QUALITY_INDIFFERENT;
            if(mob.charStats().getBodyPart(Race.BODY_LEG)<=0)
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
                if(CMLib.flags().isStanding((MOB)target))
                    return Ability.QUALITY_INDIFFERENT;
            }
         }
         return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to rend!");
			return false;
		}
		if(!Druid_ShapeShift.isShapeShifted(mob))
		{
			mob.tell("You must be in your animal form to rend.");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_LEG)<=0)
		{
			mob.tell("You must have legs to rend!");
			return false;
		}
		Ability A=mob.fetchEffect("Fighter_Pin");
		if(A!=null)
		{
			mob.tell("You rend your way out of the pin!");
			A.unInvoke();
			mob.delEffect(A);
			CMLib.commands().postStand(mob,true);
			return true;
		}

		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(CMLib.flags().isStanding(target))
		{
			mob.tell("You can only rend someone who is on the ground!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,mob.charStats().getStat(CharStats.STAT_STRENGTH)-target.charStats().getStat(CharStats.STAT_STRENGTH)-10,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			int topDamage=(adjustedLevel(mob,asLevel)+getX2Level(mob))*2;
			int damage=CMLib.dice().roll(1,topDamage,adjustedLevel(mob,asLevel));
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));
				CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,"^F^<FIGHT^><S-NAME> <DAMAGE> <T-NAME> by rending <T-HIM-HER> with <S-HIS-HER> feet!^</FIGHT^>^?");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to rend <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
