package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SenseAlignment extends Prayer
{
	public Prayer_SenseAlignment()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sense Alignment";

		quality=Ability.OK_SELF;

		baseEnvStats().setLevel(1);

		// lets save this one for druids.. clerics have the detect evil/good spells
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_SenseAlignment();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> peer(s) into the eyes of <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.tell(mob,target,"<T-NAME> seem(s) like "+target.charStats().heshe()+" is "+ExternalPlay.alignmentStr(target.getAlignment())+".");
			}
		}
		else
		{
			// it didn't work, but tell everyone you tried.
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> peer(s) into the eyes of <T-NAMESELF>, but then blink(s).");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}


		// return whether it worked
		return success;
	}
}
