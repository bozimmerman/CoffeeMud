package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_CaveIn extends StdTrap
{
	public String ID() { return "Trap_CaveIn"; }
	public String name(){ return "cave-in";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 22;}
	public String requiresToSet(){return "100 pounds of wood";}
	public Environmental newInstance(){	return new Trap_CaveIn();}
	public int baseRejuvTime(int level){ return 6;}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_WOODEN);
		if(I!=null)
			super.destroyResources(mob.location(),I.material(),100);
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_WOODEN);
		if((I==null)
		||(super.findNumberOfResource(mob.location(),I.material())<100))
		{
			mob.tell("You'll need to set down at least 100 pounds of wood first.");
			return false;
		}
		if(E instanceof Room)
		{
			Room R=(Room)E;
			if(R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
			{
				mob.tell("You can only set this trap in caves.");
				return false;
			}
		}
		return true;
	}
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((sprung)
		&&(affected!=null)
		&&(!disabled())
		&&(tickDown>=0))
		{
			if(((msg.targetMinor()==Affect.TYP_LEAVE)
				||(msg.targetMinor()==Affect.TYP_ENTER)
				||(msg.targetMinor()==Affect.TYP_FLEE))
			   &&(msg.amITarget(affected)))
			{
				msg.source().tell("The cave-in prevents entry or exit from here.");
				return false;
			}
		}
		return super.okAffect(myHost,msg);
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a cave-in!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> trigger(s) a cave-in!"))
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
								ExternalPlay.postDamage(invoker(),M,this,damage,Affect.MASK_MALICIOUS|Affect.MSG_OK_ACTION,Weapon.TYPE_BASHING,"The cave-in <DAMAGE> <T-NAME>!");
							}
					}
				}
			}
		}
	}
}
