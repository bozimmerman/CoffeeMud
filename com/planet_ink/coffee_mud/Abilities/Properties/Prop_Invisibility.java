package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Invisibility extends Property
{
	private int ticksSinceLoss=100;
	public Prop_Invisibility()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Persistant Invisibility";
		canAffectCode=Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_EXITS;
	}

	public Environmental newInstance()
	{
		return new Prop_Invisibility();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if((affect.amISource(mob))&&(Util.bset(affect.sourceCode(),Affect.MASK_MALICIOUS)))
		{
			ticksSinceLoss=0;
			mob.recoverEnvStats();
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
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
		if(ticksSinceLoss>60)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
	}
}
