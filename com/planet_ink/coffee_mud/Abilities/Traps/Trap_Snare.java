package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Snare extends StdTrap
{
	public String ID() { return "Trap_Snare"; }
	public String name(){ return "snare trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 5;}
	public String requiresToSet(){return "5 pounds of cloth";}

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_CLOTH);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),5);
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_CLOTH);
			if((I==null)
			||(findNumberOfResource(mob.location(),I.material())<5))
			{
				mob.tell("You'll need to set down at least 5 pounds of cloth first.");
				return false;
			}
		}
		return true;
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) tripping a snare trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> trip(s) a snare trap and get(s) all tangled up!"))
			{
				super.spring(target);
				target.baseEnvStats().setDisposition(target.baseEnvStats().disposition()|EnvStats.IS_SITTING);
				target.recoverEnvStats();
				Ability A=CMClass.getAbility("Thief_Bind");
				Item I=CMClass.getItem("StdItem");
				I.setName("the snare");
				A.setAffectedOne(I);
				A.invoke(invoker(),target,true);
			}
		}
	}
}
