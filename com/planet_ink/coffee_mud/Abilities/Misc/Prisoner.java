package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prisoner extends StdAbility
{
	private Room jail=null;
	public Prisoner()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Prisoner";
		displayText="";
		quality=Ability.MALICIOUS;
		baseEnvStats().setLevel(10);
		canBeUninvoked=true;

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prisoner();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		if(jail==null) jail=((MOB)affected).location();
	}
	
	public boolean okAffect(Affect affect)
	{
		if(affect.sourceMinor()==affect.TYP_RECALL)
		{
			if((affect.source()!=null)&&(affect.source().location()!=null))
				affect.source().location().show(affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the magic fizzles.");
			return false;
		}
		else
		if(affect.sourceMinor()==Affect.TYP_FLEE)
		{
			affect.source().location().show(affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to escape, but fails.");
			return false;
		}
		else
		if((affect.tool()!=null)&&(affect.tool() instanceof Ability)
		   &&(affect.targetMinor()==Affect.TYP_LEAVE))
		{
			affect.source().location().show(affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to escape, but the magic fizzles.");
			return false;
		}
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("Your sentence has been served.");
	}
}
