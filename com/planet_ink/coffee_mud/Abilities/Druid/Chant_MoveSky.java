package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_MoveSky extends Chant
{
	public String ID() { return "Chant_MoveSky"; }
	public String name(){ return "Move The Sky";}
	public String displayText(){return "";}
	public int quality(){return Ability.INDIFFERENT;}
	public int overrideMana(){return 200;}
	public Environmental newInstance(){	return new Chant_MoveSky();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s), and the sky starts moving.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(mob.location().getArea().getTODCode()==Area.TIME_NIGHT)
				{
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"The moon begin(s) to descend!");
					int x=Area.A_FULL_DAY-mob.location().getArea().getTimeOfDay();
					if(CMMap.numAreas()>0) CMMap.getFirstArea().tickTock(x);
				}
				else
				{
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"The sun hurries towards the horizon!");
					int x=13-mob.location().getArea().getTimeOfDay();
					if(CMMap.numAreas()>0) CMMap.getFirstArea().tickTock(x);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but the magic fades");


		// return whether it worked
		return success;
	}
}