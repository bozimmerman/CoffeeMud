package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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
	
	public static void pickAFight(MOB monster)
	{
		if(!canBehave(monster)) return;
		for(int i=0;i<monster.location().numInhabitants();i++)
		{
			MOB mob=monster.location().fetchInhabitant(i);
			if((!mob.isMonster())
			&&(monster.location().isInhabitant(mob))
			&&(Sense.canBeSeenBy(mob,monster)))
			{
				monster.setVictim(mob);
				break;
			}
		}
	}
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB mob=affect.source();
		if(!canBehave(affecting)) return;
		MOB monster=(MOB)affecting;
		
		if((!mob.isMonster())
		&&(monster.location().isInhabitant(mob))
		&&(Sense.canBeSeenBy(mob,monster)))
			if(!monster.isInCombat())
				monster.setVictim(mob);
	}
}
