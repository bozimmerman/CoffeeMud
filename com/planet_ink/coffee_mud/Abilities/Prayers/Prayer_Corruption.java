package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Corruption extends Prayer
{
	public String ID() { return "Prayer_Corruption"; }
	public String name(){ return "Corruption";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_Corruption();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
      FullMsg msg2=new FullMsg(target,mob,this,affectType(auto)|Affect.MASK_MALICIOUS,"<S-NAME> does not seem to like <T-NAME> messing with <S-HIS-HER> head.");

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,(auto?"<T-NAME> feel(s) more evil.":"^S<S-NAME> "+prayWord(mob)+" to corrupt <T-NAMESELF>!^?"));
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
               mob.tell("Evil, vile thoughts fill your head.");
					int evilness=Dice.roll(10,adjustedLevel(mob),0);
               int targetAlignment = target.getAlignment();
               if(targetAlignment <= evilness)
                  target.setAlignment(0);
               else
                  target.setAlignment(target.getAlignment() - evilness);
               if(!target.isInCombat() && target.isMonster()) 
               {
                  if(mob.location().okAffect(mob,msg2))
                  {
                     mob.location().send(mob,msg2);
                  }
               }
				}
			}
		}
		else
      {
         if(!target.isInCombat() && target.isMonster()) 
         {
            if(mob.location().okAffect(mob,msg2))
            {
               mob.location().send(mob,msg2);
            }
         }
			return beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");
      }


		// return whether it worked
		return success;
	}
}
