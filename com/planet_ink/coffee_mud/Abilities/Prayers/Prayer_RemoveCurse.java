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
public class Prayer_RemoveCurse extends Prayer
{
	public String ID() { return "Prayer_RemoveCurse"; }
	public String name(){ return "Remove Curse";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"^SA glow surrounds <T-NAME>.^?":"^S<S-NAME> call(s) on "+hisHerDiety(mob)+" for <T-NAME> to be released from <S-HIS-HER> curse.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=Prayer_Bless.getSomething(target,true);
				Item lastI=null;
				while(I!=null)
				{
					if(lastI==I)
					{
						FullMsg msg2=new FullMsg(target,I,null,CMMsg.MASK_GENERAL|CMMsg.MSG_DROP,"<S-NAME> release(s) <T-NAME>.");
						target.location().send(target,msg2);
					}
					else
					{
						Sense.setRemovable(I,true);
						Sense.setDroppable(I,true);
					}
					Prayer_Bless.endLowerCurses(I,adjustedLevel(mob));
					I.recoverEnvStats();
					lastI=I;
					I=Prayer_Bless.getSomething(target,true);
				}
				Prayer_Bless.endLowerCurses(target,adjustedLevel(mob));
				target.recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> call(s) on "+hisHerDiety(mob)+" to release <T-NAME> from <T-HIS-HER> curse, but nothing happens.");


		// return whether it worked
		return success;
	}
}
