package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Boomerang extends StdTrap
{
	public String ID() { return "Trap_Boomerang"; }
	public String name(){ return "boomerang";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 24;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_Boomerang();}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			boolean ok=((invoker()!=null)&&(invoker().location()!=null));
			if((!ok)||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> foil(s) a trap on "+affected.displayName()+"!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> set(s) off a trap!"))
			{
				super.spring(target);
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
				if(affected instanceof Item)
				{
					((Item)affected).remove();
					((Item)affected).removeThis();
					invoker().addInventory((Item)affected);
				}
			}
		}
	}
}
