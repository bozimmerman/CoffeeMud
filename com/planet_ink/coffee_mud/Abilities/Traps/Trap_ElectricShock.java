package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_ElectricShock extends StdTrap
{
	public String ID() { return "Trap_ElectricShock"; }
	public String name(){ return "electric shock";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 19;}
	public String requiresToSet(){return "10 pounds of metal";}
	public Environmental newInstance(){	return new Trap_ElectricShock();}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
		if(I!=null)
			super.destroyResources(mob.location(),I.material(),10);
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
		if((I==null)
		||(super.findNumberOfResource(mob.location(),I.material())<10))
		{
			mob.tell("You'll need to set down at least 10 pounds of metal first.");
			return false;
		}
		return true;
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a shocking trap!");
			else
			if(invoker().mayIFight(target))
				if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> set(s) off an shocking trap!"))
				{
					super.spring(target);
					ExternalPlay.postDamage(invoker(),target,null,Dice.roll(trapLevel(),8,1),Affect.MASK_GENERAL|Affect.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"The shock <DAMAGE> <T-NAME>!");
					if((canBeUninvoked())&&(affected instanceof Item))
						disable();
				}
		}
	}
}
