package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_Faithless extends Prayer
{
	public String ID() { return "Prayer_Faithless"; }
	public String name(){ return "Faithless";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int overrideMana(){return 100;}
	private String godName="";

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((!auto)&&(target.charStats().getCurrentClass().baseClass().equals("Cleric")))
		{
			mob.tell(target.name()+" can not be affected by this prayer.");
			return false;
		}
		if(Sense.isAnimalIntelligence(target)||Sense.isGolem(target))
		{
			if(!auto)mob.tell(target.name()+" can not be affected by this prayer.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-(levelDiff*25),auto);
		Deity D=null;
		if(target.getWorshipCharID().length()>0)
			D=CMMap.getDeity(target.getWorshipCharID());
		int type=affectType(auto);
		int mal=CMMsg.MASK_MALICIOUS;
		if(auto){ type=Util.unsetb(type,CMMsg.MASK_MALICIOUS); mal=0;}
		if((success)&&(D!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,type,auto?"":"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to lose faith!^?");
			FullMsg msg2=new FullMsg(target,D,this,CMMsg.MSG_REBUKE,"<S-NAME> LOSE(S) FAITH!!!");
			FullMsg msg3=new FullMsg(mob,target,this,CMMsg.MSK_CAST_VERBAL|mal|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))
			&&(mob.location().okMessage(mob,msg3))
			&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg3);
				if((msg.value()<=0)&&(msg3.value()<=0))
					mob.location().send(mob,msg2);
			}
		}
		else
			maliciousFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}