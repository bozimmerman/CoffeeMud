package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class Aggressive extends StdBehavior
{
	public String ID(){return "Aggressive";}
	protected int tickWait=0;
	protected int tickDown=0;
	
	public Behavior newInstance()
	{
		return new Aggressive();
	}
	public boolean grantsAggressivenessTo(MOB M)
	{
		return ExternalPlay.zapperCheck(getParms(),M);
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=getParmVal(newParms,"delay",0);
		tickDown=tickWait;
	}
	
	public static boolean startFight(MOB monster, MOB mob, boolean fightMOBs)
	{
		if((mob!=null)
		&&(monster!=null)
		&&(mob!=monster)
		&&((!mob.isMonster())||(fightMOBs))
		&&(monster.location()!=null)
		&&(monster.location().isInhabitant(mob))
		&&(canFreelyBehaveNormal(monster))
		&&(Sense.canBeSeenBy(mob,monster))
		&&(!mob.isASysOp(mob.location())))
		{
			// special backstab sneak attack!
			if(Sense.isHidden(monster))
			{
				Ability A=monster.fetchAbility("Thief_BackStab");
				if(A!=null)
				{
					A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
					A.invoke(monster,mob,false);
				}
			}
			
			// normal attack
			ExternalPlay.postAttack(monster,mob,monster.fetchWieldedItem());
			return true;
		}
		return false;
	}
	public static boolean pickAFight(MOB observer, Behavior B)
	{
		if(!canFreelyBehaveNormal(observer)) return false;
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB mob=observer.location().fetchInhabitant(i);
			if((mob!=null)
			&&(mob!=observer)
			&&(ExternalPlay.zapperCheck(B.getParms(),mob)))
			{
				if(startFight(observer,mob,false))
					return true;
			}
		}
		return false;
	}

	public static void tickAggressively(Tickable ticking, 
										int tickID,
										Behavior B)
	{
		if(tickID!=Host.MOB_TICK) return;
		if(ticking==null) return;
		if(!(ticking instanceof MOB)) return;

		pickAFight((MOB)ticking,B);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return true;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickAggressively(ticking,tickID,this);
		}
		return true;
	}
}
