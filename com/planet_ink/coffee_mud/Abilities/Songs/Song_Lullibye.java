package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Lullibye extends Song
{
	public String ID() { return "Song_Lullibye"; }
	public String name(){ return "Lullaby";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_Lullibye();	}

	boolean asleep=false;
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;
		if(asleep)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		boolean oldasleep=asleep;
		if(Dice.rollPercentage()>50)
			asleep=true;
		else
			asleep=false;
		if(asleep!=oldasleep)
		{
			if(oldasleep)
			{
				if(Sense.isSleeping(mob))
					mob.envStats().setDisposition(mob.envStats().disposition()-EnvStats.IS_SLEEPING);
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> wake(s) up.");
			}
			else
			{
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> fall(s) asleep.");
				mob.envStats().setDisposition(mob.envStats().disposition()|EnvStats.IS_SLEEPING);
			}
		}

		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if(affect.source()==invoker)
			return true;

		if(affect.source()!=affected)
			return true;


		if((!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
		&&(affect.targetMinor()==Affect.TYP_STAND)&&(asleep))
			return false;
		return true;
	}
}
