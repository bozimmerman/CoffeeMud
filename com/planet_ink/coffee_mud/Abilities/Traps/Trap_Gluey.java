package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Gluey extends StdTrap
{
	public String ID() { return "Trap_Gluey"; }
	public String name(){ return "gluey";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 11;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_Gluey();}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> clean(s) off "+affected.name()+"!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> notice(s) something about "+affected.name()+" .. it's kinda sticky."))
			{
				super.spring(target);
				if(affected instanceof Item)
				{
					((Item)affected).setRemovable(false);
					((Item)affected).setDroppable(false);
				}
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
