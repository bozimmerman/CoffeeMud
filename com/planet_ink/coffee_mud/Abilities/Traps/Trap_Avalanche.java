package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_Avalanche extends StdTrap
{
	public String ID() { return "Trap_Avalanche"; }
	public String name(){ return "avalanche";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 25;}
	public String requiresToSet(){return "100 pounds of stone";}
	public Environmental newInstance(){	return new Trap_Avalanche();}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),100);
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<100))
			{
				mob.tell("You'll need to set down at least 100 pounds of stone first.");
				return false;
			}
		}
		if(E instanceof Room)
		{
			Room R=(Room)E;
			if(R.domainType()!=Room.DOMAIN_OUTDOORS_MOUNTAINS)
			{
				if(mob!=null)
					mob.tell("You can only set this trap in the mountains.");
				return false;
			}
		}
		return true;
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off an avalanche!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> trigger(s) an avalanche!"))
			{
				super.spring(target);
				if((affected!=null)
				&&(affected instanceof Room))
				{
					Room R=(Room)affected;
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=invoker()))
							if(invoker().mayIFight(M))
							{
								int damage=Dice.roll(trapLevel(),20,1);
								ExternalPlay.postDamage(invoker(),M,this,damage,Affect.MASK_MALICIOUS|Affect.MSG_OK_ACTION,Weapon.TYPE_BASHING,"The avalanche <DAMAGE> <T-NAME>!");
							}
					}
				}
			}
		}
	}
}
