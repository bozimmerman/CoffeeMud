package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_AlterTime extends Chant
{
	public String ID() { return "Chant_AlterTime"; }
	public String name(){ return "Alter Time";}
	public String displayText(){return "";}
	public int quality(){return Ability.INDIFFERENT;}
	public Environmental newInstance(){	return new Chant_AlterTime();}

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
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s), and reality seems to start blurring.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				int x=Util.s_int(text());
				while(x==0)	x=Dice.roll(1,3,-2);
				if(x>0)
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"Time moves forwards!");
				else
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"Time moves backwards!");
				if(CMMap.numAreas()>0) CMMap.getFirstArea().tickTock(x);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but the magic fades");


		// return whether it worked
		return success;
	}
}