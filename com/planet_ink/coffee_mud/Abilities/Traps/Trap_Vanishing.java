package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Vanishing extends StdTrap
{
	public String ID() { return "Trap_Vanishing"; }
	public String name(){ return "vanishing trap";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 24;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_Vanishing();}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> foil(s) a trap on "+affected.name()+"!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> notice(s) something about "+affected.name()+" .. it's fading away."))
			{
				super.spring(target);
				affected.baseEnvStats().setDisposition(affected.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);
				affected.recoverEnvStats();
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
