package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Greasy extends StdTrap
{
	public String ID() { return "Trap_Greasy"; }
	public String name(){ return "greasy";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 10;}
	public String requiresToSet(){return "a container of lamp oil";}
	public Environmental newInstance(){	return new Trap_Greasy();}
	int times=20;
	
	private Item getPoison(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)
			&&(I instanceof Drink)
			&&(((Drink)I).containsDrink())
			&&(((Drink)I).liquidType()==EnvResource.RESOURCE_LAMPOIL))
				return I;
		}
		return null;
	}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=getPoison(mob);
		if((I!=null)&&(I instanceof Drink))
		{
			((Drink)I).setLiquidHeld(0);
			I.destroyThis();
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=getPoison(mob);
		if(I==null)
		{
			mob.tell("You'll need to set down a container of lamp oil first.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a trap!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> set(s) off a trap! "+Util.capitalize(affected.name())+" ignites!"))
			{
				super.spring(target);
				target.location().show(target,this,null,Affect.MSG_DROP,"<S-NAME> drop(s) the greasy <T-NAME>!");
				if(((--times)<=0)&&(canBeUninvoked())&&(affected instanceof Item))
					disable();
				else
					sprung=false;
			}
		}
	}
}
