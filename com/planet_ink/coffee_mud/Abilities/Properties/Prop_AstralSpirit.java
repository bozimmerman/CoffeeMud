package com.planet_ink.coffee_mud.Abilities.Properties;

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
public class Prop_AstralSpirit extends Property
{
	public String ID() { return "Prop_AstralSpirit"; }
	public String name(){ return "Astral Spirit";}
	public String displayText(){ return "(Spirit Form)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public boolean autoInvocation(MOB mob)
	{
		if((mob!=null)&&(mob.fetchEffect(ID())==null))
		{
			mob.addNonUninvokableEffect(this);
			return true;
		}
		return false;
	}

	public String accountForYourself()
	{ return "an astral spirit";	}

	public void peaceAt(MOB mob)
	{
		Room room=mob.location();
		if(room==null) return;
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB inhab=room.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.getVictim()==mob))
				inhab.setVictim(null);
		}
	}
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL)))
		{
			if((msg.tool()!=null)&&(msg.tool().ID().equalsIgnoreCase("Skill_Revoke")))
			   return super.okMessage(myHost,msg);
			else
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				mob.tell("You are unable to attack in this incorporeal form.");
				peaceAt(mob);
				return false;
			}
			else
			if((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOUTH)))
			{
				if(Util.bset(msg.sourceMajor(),CMMsg.MASK_SOUND))
					mob.tell("You are unable to make sounds in this incorporeal form.");
				else
					mob.tell("You are unable to do that this incorporeal form.");
				peaceAt(mob);
				return false;
			}
		}
		else
		if((msg.amITarget(mob))&&(!msg.amISource(mob))
		   &&(!Util.bset(msg.targetMajor(),CMMsg.MASK_GENERAL)))
		{
			mob.tell(mob.name()+" doesn't seem to be here.");
			return false;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
	}
}
