package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Bury extends Chant
{
	public String ID() { return "Chant_Bury"; }
	public String name(){ return "Bury";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance()	{	return new Chant_Bury();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This chant does not work here.");
			return false;
		}
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!(target instanceof DeadBody))
		{
			mob.tell("You may only bury the dead.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> bur(ys) <T-HIM-HERSELF>.":"^S<S-NAME> bur(ys) <T-NAMESELF> while chanting, returning dust to dust.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				target.destroyThis();
				if((mob.getAlignment()>=350)&&(mob.getAlignment()<=650))
				{
					double exp=10.0;
					int levelLimit=CommonStrings.getIntVar(CommonStrings.SYSTEMI_EXPRATE);
					int levelDiff=mob.envStats().level()-target.envStats().level();
					if(levelDiff>levelLimit) exp=0.0;
					mob.charStats().getCurrentClass().gainExperience(mob,null,null,(int)Math.round(exp));
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to bury <T-NAMESELF> while chanting, but fail(s).");

		// return whether it worked
		return success;
	}
}
