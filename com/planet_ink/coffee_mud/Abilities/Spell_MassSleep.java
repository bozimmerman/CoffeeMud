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

public class Spell_MassSleep extends Spell
	implements CharmDevotion
{
	public Spell_MassSleep()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Sleep";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Sleep)";


		malicious=true;

		baseEnvStats().setLevel(10);

		addQualifyingClass(new Mage().ID(),10);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MassSleep();
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
				mob.tell("You are way too drowsy.");
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
		Hashtable h=null;
		if(mob.isInCombat())
			h=TheFight.allCombatants(mob);
		else
			h=TheFight.allPossibleCombatants(mob);

		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth putting to sleep.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> whisper(s) and wave(s) <S-HIS-HER> arms.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// if they can't hear the sleep spell, it
				// won't happen
				if(Sense.canBeHeardBy(mob,target))
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,null);
					if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						if(!msg.wasModified())
						{
							success=maliciousAffect(mob,target,0,Affect.STRIKE_MIND);
							if(success)
								mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> fall(s) asleep!!");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> whisper(s) a sleeping spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}