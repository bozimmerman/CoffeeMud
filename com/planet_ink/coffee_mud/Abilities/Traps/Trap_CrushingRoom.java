package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_CrushingRoom extends StdTrap
{
	public String ID() { return "Trap_CrushingRoom"; }
	public String name(){ return "cave-in";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 24;}
	public String requiresToSet(){return "100 pounds of stone";}
	public Environmental newInstance(){	return new Trap_CrushingRoom();}
	public int baseRejuvTime(int level){ return 16;}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
		if(I!=null)
			super.destroyResources(mob.location(),I.material(),100);
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_ROCK);
		if((I==null)
		||(super.findNumberOfResource(mob.location(),I.material())<100))
		{
			mob.tell("You'll need to set down at least 100 pounds of stone first.");
			return false;
		}
		if(E instanceof Room)
		{
			Room R=(Room)E;
			if((R.domainType()&Room.INDOORS)==0)
			{
				mob.tell("You can only set this trap indoors.");
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
				||(msg.targetMinor()==Affect.TYP_FLEE))
			   &&(msg.amITarget(affected)))
			{
				msg.source().tell("The exits are blocked! You can't get out!");
				return false;
			}
			else
			if((msg.targetMinor()==Affect.TYP_ENTER)
			   &&(msg.amITarget(affected)))
			{
				msg.source().tell("The entry to that room is blocked!");
				return false;
			}
		}
		return super.okAffect(myHost,msg);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.TRAP_RESET)&&(getReset()>0))
		{
			if((sprung)
			&&(affected!=null)
			&&(affected instanceof Room)
			&&(!disabled())
			&&(tickDown>=0))
			{
				Room R=(Room)affected;
				if(tickDown>13)
					R.showHappens(Affect.MSG_OK_VISUAL,"The walls start closing in around you!");
				else
				if(tickDown>4)
				{
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=invoker()))
						{
							int damage=Dice.roll(trapLevel(),30,1);
							ExternalPlay.postDamage(invoker(),M,this,damage,Affect.MASK_MALICIOUS|Affect.MSG_OK_ACTION,Weapon.TYPE_BASHING,"The crushing walls <DAMAGE> <T-NAME>!");
						}
					}
				}
				else
				{
					R.showHappens(Affect.MSG_OK_VISUAL,"The walls begin retracting...");
				}
			}
		}
		return super.tick(ticking,tickID);
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a trap!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> trigger(s) a trap!"))
			{
				super.spring(target);
				target.location().showHappens(Affect.MSG_OK_VISUAL,"The exits are blocked off! The walls start closing in!");
			}
		}
	}
}
