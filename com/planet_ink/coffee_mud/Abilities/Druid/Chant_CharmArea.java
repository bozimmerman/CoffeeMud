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

public class Chant_CharmArea extends Chant
{
	public String ID() { return "Chant_CharmArea"; }
	public String name(){ return "Charm Area";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(affected)&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		   &&(!msg.amISource(invoker))
		   &&(msg.source().amFollowing()!=invoker))
		{
			msg.source().tell("You really don't feel like leaving this place.  It is just too beautiful.");
			return false;
		}
		if((Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		{
			if((msg.source()!=null)
			   &&(msg.target()!=null)
			   &&(msg.source()!=msg.target()))
			{
				msg.source().tell("You feel too peaceful here.");
				if(msg.source().getVictim()!=null)
					msg.source().getVictim().makePeace();
				msg.source().makePeace();
			}
			msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"");
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(affected)&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
		{
			msg.addTrailerMsg(new FullMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,"There is something charming about this place."));
		}
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already charmed.");
			return false;
		}
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		&&(!auto))
		{
			mob.tell("This chant does not work here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"This area seems to twinkle with beauty.":"^S<S-NAME> chant(s), bringing forth the natural beauty of this place.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but the magic fades.");

		// return whether it worked
		return success;
	}
}
