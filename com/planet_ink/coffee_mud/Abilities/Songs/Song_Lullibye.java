package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Lullibye extends Song
{
	boolean asleep=false;
	
	public Song_Lullibye()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Lullibye";
		displayText="(Song of Lullibye)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;
		mindAttack=true;

		baseEnvStats().setLevel(22);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Lullibye();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;
		if(asleep)
			affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SLEEPING);
	}
	

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
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
					mob.envStats().setDisposition(mob.envStats().disposition()-Sense.IS_SLEEPING);
				mob.location().show(mob,null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> wake(s) up.");
			}
			else
			{
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> fall(s) asleep.");
				mob.envStats().setDisposition(mob.envStats().disposition()|Sense.IS_SLEEPING);
			}
		}
		
		return true;
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(affect.source()==invoker)
			return true;

		if(affect.source()!=affected)
			return true;


		if((!Util.bset(affect.targetMajor(),Affect.ACT_GENERAL))
		&&(affect.targetMinor()==Affect.TYP_STAND)&&(asleep))
			return false;
		return true;
	}
}
