package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thiefness extends CombatAbilities
{
	public String ID(){return "Thiefness";}
	private int tickDown=0;
	public Behavior newInstance()
	{
		return new Thiefness();
	}

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		String className="Thief";
		if((getParms().length()>0)&&(CMClass.getCharClass(getParms())!=null))
			className=getParms();
		if(!mob.baseCharStats().getCurrentClass().ID().equals(className))
		{
			mob.baseCharStats().setCurrentClass(CMClass.getCharClass(className));
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return true;
		if(!canActAtAll(ticking)) return true;
		if(!(ticking instanceof MOB)) return true;
		MOB mob=(MOB)ticking;
		if((--tickDown)<=0)
		if((Dice.rollPercentage()<10)&&(mob.location()!=null))
		{
			tickDown=2;
			MOB victim=null;
			if(mob.isInCombat())
				victim=mob.getVictim();
			else
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB potentialVictim=mob.location().fetchInhabitant(i);
				if((potentialVictim!=null)
				   &&(potentialVictim!=mob)
				   &&(!potentialVictim.isMonster())
				   &&(Sense.canBeSeenBy(potentialVictim,mob)))
					victim=potentialVictim;
			}
			if(victim!=null)
			{
				Vector V=new Vector();
				Ability A=mob.fetchAbility("Thief_Steal");
				if(A==null)
					A=mob.fetchAbility("Thief_Swipe");
				else
				{
					Item I=null;
					for(int i=0;i<victim.inventorySize();i++)
					{
						Item potentialI=victim.fetchInventory(i);
						if((potentialI!=null)
						&&(potentialI.amWearingAt(Item.INVENTORY))
						&&(Sense.canBeSeenBy(potentialI,mob)))
							I=potentialI;
					}
					if(I!=null)
						V.addElement(I.ID());
				}
				V.addElement(victim.name());
				if(A!=null)
				{
					A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
					A.invoke(mob,V,null,false);
				}
			}
		}
		return true;
	}
}