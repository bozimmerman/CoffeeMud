package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Prayer_CreateWater extends Prayer
{
	private Room SpringLocation=null;
	private Item littleSpring=null;

	public Prayer_CreateWater()
	{
		super();
		isNeutral=true;
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Create Water";
		baseEnvStats().setLevel(5);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
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
		SpringLocation.show(invoker,null,Affect.VISUAL_WNOISE,"The little spring dries up.");
		super.unInvoke();
		littleSpring.destroyThis();
		SpringLocation.recoverRoomStats();
		SpringLocation=null;
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0);
		FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> call(s) to <S-HIS-HER> god for water.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				String itemID = "Spring";

				Item newItem=(Item)MUD.getItem(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				newItem=(Item)newItem.newInstance();
				mob.location().addItem(newItem);
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,"Suddenly, "+newItem.name()+" starts flowing here.");
				SpringLocation=mob.location();
				littleSpring=newItem;
				beneficialAffect(mob,newItem,0);
				mob.location().recoverEnvStats();
			}
			else
				return beneficialFizzle(mob,null,"<S-NAME> call(s) to <S-HIS-HER> god for water, but there is no answer.");
		}

		// return whether it worked
		return success;
	}
}
