package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Tripline extends StdTrap
{
	public String ID() { return "Trap_Tripline"; }
	public String name(){ return "tripline";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 1;}
	public String requiresToSet(){return "a pound of cloth";}
	public Environmental newInstance(){	return new Trap_Tripline();}
	
	public int baseRejuvTime(int level){return 2;}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_CLOTH);
		if(I!=null) I.destroyThis();
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(findMostOfMaterial(mob.location(),EnvResource.MATERIAL_CLOTH)==null)
		{
			mob.tell("You'll need to set down at least a pound of cloth first.");
			return false;
		}
		return true;
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) tripping on a taut rope!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> trip(s) on a taut rope!"))
			{
				super.spring(target);
				target.baseEnvStats().setDisposition(target.baseEnvStats().disposition()|EnvStats.IS_SITTING);
				target.recoverEnvStats();
			}
		}
	}
}
