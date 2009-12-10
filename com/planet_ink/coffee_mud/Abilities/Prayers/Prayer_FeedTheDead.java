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
public class Prayer_FeedTheDead extends Prayer
{
	public String ID() { return "Prayer_FeedTheDead"; }
	public String name(){ return "Feed The Dead";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_DEATHLORE;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_NOORDERING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int amount=100;
		if(!auto)
		{
			if((commands.size()==0)||(!CMath.isNumber((String)commands.lastElement())))
			{
				mob.tell("Feed how much experience?");
				return false;
			}
			amount=CMath.s_int((String)commands.lastElement());
			if((amount<=0)||((amount>mob.getExperience())
			&&(!CMSecurity.isDisabled("EXPERIENCE"))
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless()))
			{
				mob.tell("You cannot feed "+amount+" experience.");
				return false;
			}
			commands.removeElementAt(commands.size()-1);
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!target.charStats().getMyRace().racialCategory().equals("Undead"))
		{
			mob.tell("Only the undead may be fed in this way.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"<T-NAME> gain(s) fake life!":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to be fed.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.leveler().postExperience(mob,null,null,-amount,false);
				if((mob.envStats().level()>target.envStats().level())&&(target.isMonster()))
					amount+=(mob.envStats().level()-target.envStats().level())
						  *(mob.envStats().level()/10)
						  *amount;
				CMLib.leveler().postExperience(target,null,null,amount,false);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to be fed, but nothing happens.");


		// return whether it worked
		return success;
	}
}
