package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bomb_Water extends StdBomb
{
	public String ID() { return "Bomb_Water"; }
	public String name(){ return "water bomb";}
	protected int trapLevel(){return 1;}
	public String requiresToSet(){return "a water container";}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Drink))
		||(((Drink)E).liquidHeld()!=((Drink)E).liquidRemaining())
		||(((Drink)E).liquidType()!=EnvResource.RESOURCE_FRESHWATER))
		{
			if(mob!=null)
				mob.tell("You need a full water container to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the water bomb!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,affected.name()+" explodes water all over <T-NAME>!"))
			{
				super.spring(target);
				CoffeeUtensils.extinguish(invoker(),target,true);
			}
		}
	}

}
