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

public class Spell_Hold extends Spell
	implements EnchantmentDevotion
{
	public Spell_Hold()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hold";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Hold spell)";


		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(15);

		addQualifyingClass(new Mage().ID(),15);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Hold();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_MOVE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("You can move again!");
		Movement.standIfNecessary(mob);
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// sleep has a 10 level difference for PCs, so check for this.
		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>=10)
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		// if they can't hear the sleep spell, it
		// won't happen
		if(!Sense.canBeHeardBy(mob,target))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;


		boolean success=profficiencyCheck(-(target.envStats().level()*3));

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> encant(s) to <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,-1);
					if(success)
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> become(s) perfectly still!!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> encant(s) to <T-NAME>, but the spell fades.");

		// return whether it worked
		return success;
	}
}
