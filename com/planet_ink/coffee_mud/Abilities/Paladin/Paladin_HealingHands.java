package com.planet_ink.coffee_mud.Abilities.Paladin;
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
public class Paladin_HealingHands extends StdAbility
{
	public String ID() { return "Paladin_HealingHands"; }
	public String name(){ return "Healing Hands";}
	private static final String[] triggerStrings = {"HANDS"};
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_HEALING;}
	public long flags(){return Ability.FLAG_HEALINGMAGIC;}
    protected long minCastWaitTime(){return Tickable.TIME_TICK;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!CMLib.flags().aliveAwakeMobileUnbound(mob,false))
			return false;

		if((!auto)&&(!(CMLib.flags().isGood(mob))))
		{
			mob.tell("Your alignment has alienated your god from you.");
			return false;
		}

		int healing=1+((int)Math.round(CMath.div(adjustedLevel(mob,asLevel),5.0)));
		if(mob.curState().getMana()<healing)
		{
			mob.tell("You don't have enough mana to do that.");
			return false;
		}

		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		helpProficiency(mob);

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_CAST_SOMANTIC_SPELL,auto?"A pair of celestial hands surround <T-NAME>":"^S<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int manaLost=healing;
				if(manaLost>0) manaLost=manaLost*-1;
				mob.curState().adjMana(manaLost,mob.maxState());
				CMLib.combat().postHealing(mob,target,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,healing,null);
				target.tell("You feel a little better!");
                lastCastHelp=System.currentTimeMillis();
			}
		}
		else
			return beneficialVisualFizzle(mob,mob,"<S-NAME> lay(s) <S-HIS-HER> healing hands onto <T-NAMESELF>, but <S-HIS-HER> god does not heed.");


		// return whether it worked
		return success;
	}

}
