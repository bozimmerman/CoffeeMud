package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_MonsterCage extends StdTrap
{
	public String ID() { return "Trap_MonsterCage"; }
	public String name(){ return "monster cage";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 10;}
	public String requiresToSet(){return "a caged monster";}
	public Environmental newInstance(){	return new Trap_MonsterCage();}
	
	private MOB monster=null;
	
	private Item getCagedAnimal(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if(I instanceof CagedAnimal)
			{
				MOB M=((CagedAnimal)I).unCageMe();
				if(M!=null) return I;
			}
		}
		return null;
	}
	
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Item I=getCagedAnimal(mob);
		if(I!=null) I.destroy();
		setMiscText(((CagedAnimal)I).cageText());
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.TRAP_RESET)&&(getReset()>0))
		{
			// recage the motherfather
			if((tickDown<=1)
			&&(monster!=null)
			&&(monster.amDead()||(!monster.isInCombat())))
				monster.destroy();
		}
		return super.tick(ticking,tickID);
	}
	
	
	public void unInvoke()
	{
		if((monster!=null)&&(canBeUninvoked()))
			monster.destroy();
		super.unInvoke();
	}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(getCagedAnimal(mob)==null)
		{
			if(mob!=null)
				mob.tell("You'll need to set down a caged animal of some sort first.");
			return false;
		}
		return true;
	}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null)&&(text().length()>0))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) opening a monster cage!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> trip(s) open a caged monster!"))
			{
				super.spring(target);
				Item I=CMClass.getItem("GenCaged");
				((CagedAnimal)I).setCageText(text());
				monster=((CagedAnimal)I).unCageMe();
				if(monster!=null)
				{
					monster.baseEnvStats().setRejuv(0);
					monster.bringToLife(target.location(),true);
					monster.setVictim(target);
					if(target.getVictim()==null)
						target.setVictim(monster);
				}
			}
		}
	}
}
