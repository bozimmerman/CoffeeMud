package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Noise extends StdTrap
{
	public String ID() { return "Trap_Noise"; }
	public String name(){ return "noisy trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 1;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_Noise();}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a noise trap!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> set(s) off a noise trap!"))
			{
				super.spring(target);
				Area A=target.location().getArea();
				for(Enumeration e=A.getMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					if(R!=target.location())
						R.showHappens(Affect.MASK_GENERAL|Affect.MSG_NOISE,"You hear a loud noise coming from somewhere.");
				}
				if(canBeUninvoked())
					disable();
			}
		}
	}
}
