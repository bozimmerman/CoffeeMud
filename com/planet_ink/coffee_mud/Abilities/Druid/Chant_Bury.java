package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Bury extends Chant
{
	public Chant_Bury()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Bury";

		baseEnvStats().setLevel(3);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_Bury();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY))
		{
			mob.tell("This chant does not work in the city.");
			return false;
		}
		Item target=getTarget(mob,mob.location(),givenTarget,commands);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> bury(s) <T-HIM-HER>self.":"<S-NAME> bury(s) <T-NAMESELF> while chanting, returning dust to dust.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				target.destroyThis();
				if((mob.getAlignment()>=350)&&(mob.getAlignment()<=650))
					mob.charStats().getMyClass().gainExperience(mob,null,null,10);
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to bury <T-NAMESELF> while chanting, but fail(s).");

		// return whether it worked
		return success;
	}
}
