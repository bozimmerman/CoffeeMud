package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_BearTrap extends StdTrap
{
	public String ID() { return "Trap_BearTrap"; }
	public String name(){ return "bear trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 16;}
	public String requiresToSet(){return "30 pounds of metal";}
	public Environmental newInstance(){	return new Trap_BearTrap();}
	public int baseRejuvTime(int level){ return 35;}

	private int amountRemaining=250;
	private MOB trapped=null;

	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
			if(I==null) I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_MITHRIL);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),30);
		}
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Item I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
		if(I==null)	I=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_MITHRIL);
		if(mob!=null)
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<30))
			{
				mob.tell("You'll need to set down at least 30 pounds of metal first.");
				return false;
			}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((sprung)
		&&(trapped!=null)
		&&(affected!=null)
		&&(msg.amISource(trapped))
		&&(trapped.location()!=null))
		{
			if((((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.targetMinor()==CMMsg.TYP_FLEE))
				&&(msg.amITarget(affected)))
			||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||(msg.sourceMinor()==CMMsg.TYP_RETREAT))
			{
				if(trapped.location().show(trapped,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> struggle(s) to get out of the bear trap."))
				{
					amountRemaining-=trapped.charStats().getStat(CharStats.STRENGTH);
					amountRemaining-=trapped.envStats().level();
					if(amountRemaining<=0)
					{
						trapped.location().show(trapped,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> pull(s) free of the bear trap.");
						trapped=null;
					}
					else
						return false;
				}
				else
					return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void spring(MOB target)
	{
		trapped=null;
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!invoker().mayIFight(target))||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) a bear trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> step(s) on a bear trap!"))
			{
				super.spring(target);
				int damage=Dice.roll(trapLevel(),6,1);
				trapped=target;
				amountRemaining=250+(trapLevel()*10);
				ExternalPlay.postDamage(invoker(),target,this,damage,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_PIERCING,"The bear trap <DAMAGE> <T-NAME>!");
			}
		}
	}
}
