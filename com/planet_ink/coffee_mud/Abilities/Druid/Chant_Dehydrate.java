package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Dehydrate extends Chant
{
	public String ID() { return "Chant_Dehydrate"; }
	public String name(){return "Dehydrate";}
	public String displayText(){return "(Dehydrate)";}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Chant_Dehydrate();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					if(target instanceof MOB)
					{
						((MOB)target).curState().adjThirst(-150 - (mob.envStats().level() * 100),((MOB)target).maxState());
						mob.location().show(((MOB)target),null,Affect.MSG_OK_VISUAL,"<S-NAME> feel(s) incredibly thirsty!");
					}
					else
					if(target instanceof Item)
					{
						if(target instanceof Container)
						{
							Vector V=((Container)target).getContents();
							for(int i=0;i<V.size();i++)
							{
								Item I=(Item)V.elementAt(i);
								if(I instanceof Drink)
								{
									if(((Drink)I).liquidRemaining()<10000)
										((Drink)I).setLiquidRemaining(0);
									if(I instanceof EnvResource)
										I.destroy();
								}
							}
							if(target instanceof Drink)
							{
								if(((Drink)target).liquidRemaining()<10000)
									((Drink)target).setLiquidRemaining(0);
							}
							if(target instanceof EnvResource)
								((Item)target).destroy();
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
