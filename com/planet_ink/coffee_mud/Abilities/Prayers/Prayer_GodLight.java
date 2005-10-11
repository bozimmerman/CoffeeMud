package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_GodLight extends Prayer
{
	public String ID() { return "Prayer_GodLight"; }
	public String name(){ return "Godlight";}
	public String displayText(){return "(Godlight)";}
	public long flags(){return Ability.FLAG_HOLY;}
	public int quality(){return Ability.MALICIOUS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GLOWING);
        if(Util.bset(affectableStats.disposition(),EnvStats.IS_DARK))
            affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
		if(!(affected instanceof MOB)) return;
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your vision returns.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		Environmental target=null;
		if((!auto)
        &&((commands.size()==0)||(((String)commands.firstElement()).equalsIgnoreCase("ROOM")))
        &&(!mob.isInCombat()))
			target=mob.location();
		if((target==null)&&(commands.size()==0)&&(mob.isInCombat()))
			target=mob.getVictim();
		if(target==null)
			target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if((target instanceof Room)&&(target.fetchEffect(ID())!=null))
		{
			mob.tell("This place already has the god light.");
			return false;
		}

		if((target instanceof MOB)
		&&(((MOB)target).charStats().getBodyPart(Race.BODY_EYE)==0))
		{
			mob.tell(target.name()+" has no eyes, and would not be affected.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":((target instanceof MOB)?"^S<S-NAME> point(s) to <T-NAMESELF> and "+prayWord(mob)+". A beam of bright sunlight flashes into <T-HIS-HER> eyes!^?":"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+".^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(target instanceof MOB)
						mob.location().show((MOB)target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> go(es) blind!");
					maliciousAffect(mob,target,asLevel,0,-1);
                    mob.location().recoverRoomStats();
                    mob.location().recoverRoomStats();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
