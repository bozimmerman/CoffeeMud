package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CreateWater extends Prayer
{
	private Room SpringLocation=null;
	private Item littleSpring=null;

	public Prayer_CreateWater()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Create Water";
		baseEnvStats().setLevel(5);

		canAffectCode=0;
		canTargetCode=0;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_CreateWater();
	}

	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		if(canBeUninvoked)
			SpringLocation.showHappens(Affect.MSG_OK_VISUAL,"The little spring dries up.");
		super.unInvoke();
		if(canBeUninvoked)
		{
			Item spring=littleSpring; // protects against uninvoke loops!
			littleSpring=null;
			spring.destroyThis();
			SpringLocation.recoverRoomStats();
			SpringLocation=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"^S<S-NAME> call(s) to <S-HIS-HER> god for water.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				String itemID = "Spring";

				Item newItem=(Item)CMClass.getItem(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				mob.location().addItem(newItem);
				mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				SpringLocation=mob.location();
				littleSpring=newItem;
				beneficialAffect(mob,newItem,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) to <S-HIS-HER> god for water, but there is no answer.");

		// return whether it worked
		return success;
	}
}
