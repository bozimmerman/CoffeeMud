package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CreateFood extends Prayer
{
	public String ID() { return "Prayer_CreateFood"; }
	public String name(){ return "Create Food";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Prayer_CreateFood();	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for food.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=(Item)CMClass.getStdItem("StdFood");
				newItem.setBaseValue(1);
				mob.location().addItemRefuse(newItem,Item.REFUSE_RESOURCE);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for food, but there is no answer.");

		// return whether it worked
		return success;
	}
}
