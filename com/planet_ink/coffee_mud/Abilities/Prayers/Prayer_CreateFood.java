package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CreateFood extends Prayer
{
	public Prayer_CreateFood()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Create Food";
		baseEnvStats().setLevel(5);

		addQualifyingClass("Cleric",baseEnvStats().level());
		addQualifyingClass("Paladin",baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_CreateFood();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> call(s) to <S-HIS-HER> god for food.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Item newItem=(Item)CMClass.getStdItem("StdFood");
				mob.location().addItem(newItem);
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) to <S-HIS-HER> god for food, but there is no answer.");

		// return whether it worked
		return success;
	}
}
