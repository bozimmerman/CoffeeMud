package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bomb_FlameBurst extends StdBomb
{
	public String ID() { return "Bomb_FlameBurst"; }
	public String name(){ return "flame burst bomb";}
	protected int trapLevel(){return 17;}
	public String requiresToSet(){return "some lamp oil";}
	public Environmental newInstance(){	return new Bomb_FlameBurst();}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Item))
		||(!(E instanceof Drink))
		||(!((((Drink)E).containsDrink())||(((Drink)E).liquidType()!=EnvResource.RESOURCE_LAMPOIL)))
		   &&(((Item)E).material()!=EnvResource.RESOURCE_LAMPOIL))
		{
			mob.tell("You need some lamp oil to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) the flame burst!");
			else
			if(target.location().show(invoker(),target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,affected.displayName()+" flames all over <T-NAME>!"))
			{
				super.spring(target);
				ExternalPlay.postDamage(invoker(),target,null,Dice.roll(trapLevel(),12,1),Affect.MASK_GENERAL|Affect.TYP_FIRE,Weapon.TYPE_BURNING,"The flames <DAMAGE> <T-NAME>!");
			}
		}
	}
	
}
