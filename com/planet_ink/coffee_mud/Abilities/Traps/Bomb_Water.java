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
	public Environmental newInstance(){	return new Bomb_Water();}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Drink))
		||(((Drink)E).liquidHeld()!=((Drink)E).liquidRemaining())
		||(((Drink)E).liquidType()!=EnvResource.RESOURCE_FRESHWATER))
		{
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
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) the water bomb!");
			else
			if(invoker().mayIFight(target))
				if(target.location().show(invoker(),target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,affected.displayName()+" explodes water all over <T-NAME>!"))
				{
					super.spring(target);
					ExternalPlay.extinguish(invoker(),target,7);
				}
		}
	}
	
}
