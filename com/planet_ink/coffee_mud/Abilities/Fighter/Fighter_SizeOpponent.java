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
public class Fighter_SizeOpponent extends FighterSkill
{
	public String ID() { return "Fighter_SizeOpponent"; }
	public String name(){ return "Opponent Knowledge";}
	private static final String[] triggerStrings = {"SIZEUP","OPPONENT"};
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_COMBATLORE;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_LOOK|(auto?CMMsg.MASK_ALWAYS:0),"<S-NAME> size(s) up <T-NAMESELF> with <S-HIS-HER> eyes.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer buf=new StringBuffer(target.name()+" looks to have "+target.curState().getHitPoints()+" out of "+target.maxState().getHitPoints()+" hit points.\n\r");
				buf.append(target.charStats().HeShe()+" looks like "+target.charStats().heshe()+" is "+CMLib.combat().fightingProwessStr(target)+" and is "+CMLib.combat().armorStr(target)+".");
				mob.tell(buf.toString());
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> size(s) up <T-NAMESELF> with <S-HIS-HER> eyes, but look(s) confused.");

		// return whether it worked
		return success;
	}
}
