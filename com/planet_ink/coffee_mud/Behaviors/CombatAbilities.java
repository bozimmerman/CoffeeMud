package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class CombatAbilities extends StdBehavior
{
	public CombatAbilities()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	
	public Behavior newInstance()
	{ 
		return new CombatAbilities();
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		if(tickID!=ServiceEngine.MOB_TICK) return;
		if(!canBehave(ticking)) return;
		MOB mob=(MOB)ticking;
		Room thisRoom=mob.location();
		
		double aChance=Util.div(mob.curState().getMana(),mob.maxState().getMana());
		if((Math.random()>aChance)||(mob.curState().getMana()<50))
			return;
		
		int tries=0;
		Ability tryThisOne=null;
		while((tryThisOne==null)&&(tries<100)&&(mob.numAbilities()>0))
		{
			tryThisOne=mob.fetchAbility((int)Math.round(Math.random()*mob.numAbilities()));
			if((tryThisOne!=null)&&(mob.fetchAffect(tryThisOne.ID())==null))
			{
				if(tryThisOne instanceof Prayer)
				{
					Prayer thisPrayer=(Prayer)tryThisOne;
					if((!thisPrayer.appropriateToMyAlignment(mob))||(thisPrayer.isNeutral())||(!thisPrayer.isMalicious()))
						tryThisOne=null;
				}
				else
				if(!tryThisOne.isMalicious())
					tryThisOne=null;
			}
			else
				tryThisOne=null;
			tries++;
		}
		if(tryThisOne!=null)
		{
			MOB victim=mob.getVictim();
			if(victim!=null)
			{
				Vector V=new Vector();
				V.addElement(victim.name());
				tryThisOne.invoke(mob,V);
			}
		}
	}
}
