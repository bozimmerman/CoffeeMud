package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Boulders extends StdTrap
{
	public String ID() { return "Trap_Boulders"; }
	public String name(){ return "boulders";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 20;}
	public String requiresToSet(){return "50 pounds of boulders";}
	public Environmental newInstance(){	return new Trap_Boulders();}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
		if(I!=null)
			super.destroyResources(mob.location(),I.material(),50);
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
		if((I==null)
		||(super.findNumberOfResource(mob.location(),I.material())<50))
		{
			mob.tell("You'll need to set down at least 50 pounds of rock first.");
			return false;
		}
		if(E instanceof Room)
		{
			Room R=(Room)E;
			if((R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
			   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
			   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_ROCKS)
			   &&(R.domainType()!=Room.DOMAIN_OUTDOORS_HILLS))
			{
				mob.tell("You can only set this trap in caves, or by mountains or hills.");
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
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a boulder trap!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> trigger(s) a trap!"))
			{
				super.spring(target);
				int damage=Dice.roll(trapLevel(),20,1);
				ExternalPlay.postDamage(invoker(),target,this,damage,Affect.MASK_MALICIOUS|Affect.MSG_OK_ACTION,Weapon.TYPE_BASHING,"Dozens of boulders <DAMAGE> <T-NAME>!");
			}
		}
	}
}
