package com.planet_ink.coffee_mud.Abilities.Fighter;
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
public class Fighter_Cartwheel extends FighterSkill
{
	public String ID() { return "Fighter_Cartwheel"; }
	public String name(){ return "Cartwheel";}
	private static final String[] triggerStrings = {"CARTWHEEL"};
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_ACROBATIC;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB victim=mob.getVictim();
		if(victim==null)
		{
			mob.tell("You can only do this in combat!");
			return false;
		}
		if(mob.rangeToTarget()>=mob.location().maxRange())
		{
			mob.tell("You can not get any further away here!");
			return false;
		}
		if((mob.charStats().getBodyPart(Race.BODY_LEG)<=1)
		||(mob.charStats().getBodyPart(Race.BODY_ARM)<=1))
		{
			mob.tell("You need arms and legs to do this.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,victim,this,CMMsg.MSG_RETREAT,"<S-NAME> cartwheel(s) away from <T-NAMESELF>!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int tries=10;
				while(((--tries)>=0)&&(mob.rangeToTarget()<mob.location().maxRange()))
				{
					msg=CMClass.getMsg(mob,victim,this,CMMsg.MSG_RETREAT,null);
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to cartwheel and fail(s).");

		// return whether it worked
		return success;
	}
}
