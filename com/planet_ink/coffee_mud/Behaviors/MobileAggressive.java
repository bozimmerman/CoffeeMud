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

public class MobileAggressive extends Mobile
{
	
	public MobileAggressive()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{ 
		return new MobileAggressive();
	}
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		
		if(!canBehave(affecting)) return;
		MOB mob=affect.source();
		MOB monster=(MOB)affecting;
		
		if((!mob.isMonster())
		&&(monster.location().isInhabitant(mob))
		&&(Sense.canBeSeenBy(mob,monster)))
			monster.setVictim(mob);
		Aggressive.pickAFight(monster);
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		VeryAggressive.tickVeryAggressively(ticking,tickID);
	}
}
