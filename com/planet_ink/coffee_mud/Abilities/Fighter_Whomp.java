package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Fighter_Whomp extends StdAbility
{
	public Fighter_Whomp()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Whomp";
		displayText="(the great power of the warrior)";
		miscText="";

		triggerStrings.addElement("WHOMP");

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(16);
		addQualifyingClass(new Fighter().ID(),16);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Whomp();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((affect.sourceType()!=Affect.AREA)
			&&(affect.sourceType()!=Affect.GENERAL)
			&&(affect.sourceType()!=Affect.NO_EFFECT))
			{
				mob.tell(mob,null,"You are way too drowsy.");
				return false;
			}
		}
		return super.okAffect(affect);
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SLEEPING);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("You feel less drowsy.");
		Movement.standIfNecessary(mob);
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>=10)
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}


		if(mob.charStats().getStrength()<18)
		{
			mob.tell("You need at least an 18 strength to do that.");
			return false;
		}

		if(mob.envStats().weight()<(target.envStats().weight()-100))
		{
			mob.tell(target.name()+" is way to big to knock out!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob.charStats().getStrength()-target.charStats().getStrength()-10);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.STRIKE_JUSTICE,Affect.STRIKE_JUSTICE,Affect.VISUAL_WNOISE,"<S-NAME> knock(s) <T-NAME> to the floor!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to knock <T-NAME> out, but fail(s).");

		// return whether it worked
		return success;
	}
}
