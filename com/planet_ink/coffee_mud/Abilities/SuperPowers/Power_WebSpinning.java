package com.planet_ink.coffee_mud.Abilities.SuperPowers;

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
public class Power_WebSpinning extends SuperPower
{
	public String ID() { return "Power_WebSpinning"; }
	public String name(){return "Web Spinning";}
	public String displayText(){return "(Webbed)";}
	public int maxRange(){return 5;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS|CAN_EXITS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS|CAN_EXITS;}
	public long flags(){return Ability.FLAG_BINDING;}

	public int amountRemaining=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
	
			// when this spell is on a MOBs Affected list,
			// it should consistantly prevent the mob
			// from trying to do ANYTHING except sleep
			if(msg.amISource(mob))
			{
				if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
				&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
				||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
				{
					if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against the web."))
					{
						amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
						if(amountRemaining<0)
							unInvoke();
					}
					return false;
				}
			}
		}
		else
		if(affected instanceof Item)
		{
		    if(msg.target()==affected)
		    {
		        if(msg.targetMinor()==CMMsg.TYP_GET)
		            msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),null,CMMsg.MSG_OK_VISUAL,"<T-NAME> is covered in sticky webbing!",null,null));
		        else
		        if((msg.targetMinor()==CMMsg.TYP_DROP)
		        &&(((Item)affected).owner()==msg.source()))
		        {
		            msg.source().tell(msg.source(),affected,null,"<T-NAME> is too sticky to let go of!");
		            return false;
		        }
		    }
		}
		else
		if(affected instanceof Exit)
		{
		    if(msg.target()==affected)
		    {
		        if(msg.targetMinor()==CMMsg.TYP_OPEN)
		        {
		            msg.source().tell(msg.source(),affected,null,"<T-NAME> is held fast by gobs of webbing!");
		            return false;
		        }
		    }
		}
		return super.okMessage(myHost,msg);
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
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the web.");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=super.getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
		    FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,(auto?"":"^S<S-NAME> shoot(s) and spin(s) a web at <T-NAMESELF>!^?")+CommonStrings.msp("web.wav",40));
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					amountRemaining=160;
					if(CoffeeUtensils.roomLocation(target)==mob.location())
					{
						success=maliciousAffect(mob,target,asLevel,(adjustedLevel(mob,asLevel)*10),-1);
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> become(s) stuck in a mass of web!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> spin(s) a web towards <T-NAMESELF>, but miss(es).");


		// return whether it worked
		return success;
	}
}
