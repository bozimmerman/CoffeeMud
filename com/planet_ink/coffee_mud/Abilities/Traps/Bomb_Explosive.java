package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bomb_Explosive extends StdBomb
{
	public String ID() { return "Bomb_Explosive"; }
	public String name(){ return "explosive bomb";}
	protected int trapLevel(){return 18;}
	public String requiresToSet(){return "a pound of coal";}
	public Environmental newInstance(){	return new Bomb_Explosive();}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Item))
		||(((Item)E).material()!=EnvResource.RESOURCE_COAL))
		{
			mob.tell("You need some coal to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) the explosive!");
			else
			if(target.location().show(invoker(),target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,affected.name()+" explodes all over <T-NAME>!"))
			{
				super.spring(target);
				ExternalPlay.postDamage(invoker(),target,null,Dice.roll(trapLevel(),10,1),Affect.MASK_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"The blast <DAMAGE> <T-NAME>!");
			}
		}
	}
	
}
