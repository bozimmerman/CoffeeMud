package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
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


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(10);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
		maxRange=0;
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
		if((affect.amISource(mob))
		&&(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
		&&(affect.sourceMajor()>0))
		{
			mob.tell("You are way too drowsy.");
			return false;
		}
		return super.okAffect(affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
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
		ExternalPlay.standIfNecessary(mob);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth putting to sleep.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(-20,auto);

		if(success)
		{
			mob.location().show(mob,null,affectType,auto?"":"<S-NAME> whisper(s) and wave(s) <S-HIS-HER> arms.");
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
					MOB oldVictim=mob.getVictim();
					FullMsg msg=new FullMsg(mob,target,this,affectType,null);
					if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						if(!msg.wasModified())
						{
							success=maliciousAffect(mob,target,2,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND);
							if(success)
								if(target.location()==mob.location())
									target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> fall(s) asleep!!");
						}
					}
					if(oldVictim==null) mob.setVictim(null);
				}
				else
					maliciousFizzle(mob,target,"<T-NAME> seem(s) unaffected by the Sleep spell from <S-NAME>.");
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> whisper(s) a sleeping spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}