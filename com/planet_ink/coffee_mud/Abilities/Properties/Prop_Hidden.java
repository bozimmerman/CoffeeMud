package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Hidden extends Property
{
	private int ticksSinceLoss=100;
	public Prop_Hidden()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Persistant Hiddenness";
		canAffectCode=Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_EXITS;
	}

	public Environmental newInstance()
	{
		return new Prop_Hidden();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			
			if(((!Util.bset(affect.sourceMajor(),Affect.ACT_SOUND)
				 ||(affect.sourceMinor()==Affect.TYP_SPEAK)
				 ||(affect.sourceMinor()==Affect.TYP_ENTER)
				 ||(affect.sourceMinor()==Affect.TYP_LEAVE)
				 ||(affect.sourceMinor()==Affect.TYP_RECALL)))
			 &&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			 &&(affect.sourceMinor()!=Affect.TYP_EXAMINESOMETHING)
			 &&(affect.sourceMajor()>0))
			{
				ticksSinceLoss=0;
				mob.recoverEnvStats();
			}
		}
		return;
	}

	public boolean tick(int tickID)
	{
		if(ticksSinceLoss<9999)
			ticksSinceLoss++;
		return super.tick(tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
		if(ticksSinceLoss>30)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
	}
}