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
	public int overrideMana(){return 100;}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s), and reality seems to start blurring.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int x=Util.s_int(text());
				while(x==0)	x=Dice.roll(1,3,-2);
				if(x>0)
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Time moves forwards!");
				else
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Time moves backwards!");
				mob.location().getArea().getTimeObj().tickTock(x);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but the magic fades");


		// return whether it worked
		return success;
	}
}