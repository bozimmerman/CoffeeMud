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

public class Prayer_CreateFood extends Prayer
{
	public Prayer_CreateFood()
	{
		super();
		isNeutral=true;
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Create Food";
		baseEnvStats().setLevel(5);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_CreateFood();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0);
		FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> call(s) to <S-HIS-HER> god for food.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				String itemID = "Food";

				Item newItem=(Item)MUD.getItem(itemID);

				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}

				newItem=(Item)newItem.newInstance();
				mob.location().addItem(newItem);
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,"Suddenly, "+newItem.name()+" drops from the sky.");
				mob.location().recoverEnvStats();
			}
			else
			{
				// it didn't work, but tell everyone you tried.
				FullMsg msg2=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> call(s) to <S-HIS-HER> god for food, but there is no answer.");
				if(mob.location().okAffect(msg2))
					mob.location().send(mob,msg2);
			}
		}

		// return whether it worked
		return success;
	}
}
