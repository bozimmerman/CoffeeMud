package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Darkfall extends StdTrap
{
	public String ID() { return "Trap_Darkfall"; }
	public String name(){ return "darkfall";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 2;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_Darkfall();}
	
	public int baseRejuvTime(int level){return 20;}
	private boolean disabled=false;
	
	public boolean disabled(){
		return (sprung&&disabled)
			   ||(affected==null)
			   ||(affected.fetchAffect(ID())==null);
	}
	public void disable(){ 
		disabled=true;
		super.disable();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(sprung)
		{
			if(!disabled)
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_DARK);
		}
		else
			disabled=false;
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a trap!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> set(s) off a darkness trap!"))
			{
				super.spring(target);
				target.location().recoverRoomStats();
			}
		}
	}
}
