package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Prayer_Annul extends Prayer
{
	public String ID() { return "Prayer_Annul"; }
	public String name(){ return "Annul";}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public int quality(){return Ability.OK_OTHERS;}
	public Environmental newInstance(){	return new Prayer_Annul();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!target.isMarriedToLiege())
		{
			mob.tell(target.name()+" is not married!");
			return false;
		}
		if(target.fetchWornItem("wedding band")!=null)
		{
			mob.tell(target.name()+" must remove the wedding band first.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> annul(s) the marriage between <T-NAMESELF> and "+target.getLiegeID()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB M=CMMap.getPlayer(target.getLiegeID());
				if(M!=null) M.setLiegeID("");
				target.setLiegeID("");
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> clear(s) <S-HIS-HER> throat.");

		return success;
	}
}
