package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_RemoveDeathMark extends Prayer
{
	public String ID() { return "Prayer_RemoveDeathMark"; }
	public String name(){ return "Remove Death Mark";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
	public Environmental newInstance(){	return new Prayer_RemoveDeathMark();}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"^SA glow surrounds <T-NAME>.^?":"^S<S-NAME> call(s) on "+hisHerDiety(mob)+" for <T-NAME> to be released from <T-HIS-HER> death mark.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability E=target.fetchEffect("Thief_Mark");
				if(E!=null) E.unInvoke();
				E=target.fetchEffect("Thief_ContractHit");
				if(E!=null) E.unInvoke();
				target.recoverEnvStats();
				for(Enumeration e=CMMap.players();e.hasMoreElements();)
				{
					MOB M=(MOB)e.nextElement();
					if((M!=null)&&(M!=target))
					{
						E=target.fetchEffect("Thief_Mark");
						if((E!=null)&&(E.text().startsWith(target.Name()+"/")))
							E.unInvoke();
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> call(s) on "+hisHerDiety(mob)+" to release <T-NAME> from <T-HIS-HER> death mark, but nothing happens.");


		// return whether it worked
		return success;
	}
}