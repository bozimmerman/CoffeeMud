package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thiefness extends CombatAbilities
{
	public Thiefness()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Behavior newInstance()
	{
		return new Thiefness();
	}

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		if(!mob.baseCharStats().getMyClass().ID().equals("Thief"))
		{
			mob.baseCharStats().setMyClass(CMClass.getCharClass("Thief"));
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		if(!canActAtAll(ticking)) return;
		if(!(ticking instanceof MOB)) return;
		MOB mob=(MOB)ticking;
		if((Dice.rollPercentage()<10)&&(mob.location()!=null))
		{
			MOB victim=null;
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB potentialVictim=mob.location().fetchInhabitant(i);
				if((potentialVictim!=null)&&(!potentialVictim.isMonster())&&(Sense.canBeSeenBy(potentialVictim,mob)))
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
					A.setProfficiency(Dice.roll(1,50,(mob.baseEnvStats().level()-A.qualifyingLevel(mob))*15));
					A.invoke(mob,V,null,false);
				}
			}
		}
	}
}