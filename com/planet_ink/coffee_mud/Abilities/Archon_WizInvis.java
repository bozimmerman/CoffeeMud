package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;

public class Archon_WizInvis extends ArchonSkill
{
	public Archon_WizInvis()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="WizInvisible";
		displayText="(Wizard Invisibility)";

		triggerStrings.addElement("WIZINV");
	}

	public Environmental newInstance()
	{
		return new Archon_WizInvis();
	}
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SEEN);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_HIDDEN);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SNEAKING);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_FLYING);
		if(!Sense.canBreathe(affected))
			affectableStats.setSensesMask(affectableStats.sensesMask()-Sense.CAN_BREATHE);
		if(Sense.isSleeping(affected))
			affectableStats.setDisposition(affectableStats.disposition()-Sense.IS_SLEEPING);
		if(Sense.isSitting(affected))
			affectableStats.setDisposition(affectableStats.disposition()-Sense.IS_SITTING);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affected.curState().setHunger(affected.maxState().getHunger());
		affected.curState().setThirst(affected.maxState().getThirst());
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("You begin to fade back into view.");
	}



	public boolean invoke(MOB mob, Vector commands)
	{

		Ability A=mob.fetchAffect(this.ID());
		if((A!=null)&&(CommandProcessor.combine(commands,0).trim().equalsIgnoreCase("OFF")))
		{
		   A.unInvoke();
		   return true;
		}
		else
		if(A!=null)
		{
			mob.tell("You have already faded from view!");
			return false;
		}

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		invoker=mob;
		mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> fade(s) from view!");
		mob.addAffect((Ability)this.copyOf());

		mob.tell("You may uninvoke WIZINV with 'WIZINV OFF'.");
		// return whether it worked
		return true;
	}
}	