package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class Aggressive extends StdBehavior
{

	public Aggressive()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new Aggressive();
	}

	public static void startFight(MOB monster, MOB mob, boolean fightMOBs)
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
					A.setProfficiency(Dice.roll(1,50,(mob.baseEnvStats().level()-A.qualifyingLevel(mob))*15));
					A.invoke(monster,mob,false);
				}
			}
			
			// normal attack
			ExternalPlay.postAttack(monster,mob,monster.fetchWieldedItem());
		}
	}
	public static void pickAFight(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB mob=observer.location().fetchInhabitant(i);
			if((mob!=null)&&(mob!=observer))
			{
				startFight(observer,mob,false);
				if(observer.isInCombat()) break;
			}
		}
	}

	public static void tickAggressively(Environmental ticking, int tickID)
	{
		if(tickID!=Host.MOB_TICK) return;
		if(ticking==null) return;
		if(!(ticking instanceof MOB)) return;

		pickAFight((MOB)ticking);
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Host.MOB_TICK) return;
		tickAggressively(ticking,tickID);
	}
}
