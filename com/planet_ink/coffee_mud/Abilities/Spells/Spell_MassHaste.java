package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MassHaste extends Spell
{
	public Spell_MassHaste()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Haste";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Hasted)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;


		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(14);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MassHaste();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSpeed(affectableStats.speed() * 2.0);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("You begin to slow down to a normal speed.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,false);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth speeding up.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.location().show(mob,null,affectType,auto?"":"<S-NAME> wave(s) <S-HIS-HER> arms and chant(s) quickly.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,null);
				if((mob.location().okAffect(msg))
				   &&(target.fetchAffect("Spell_Haste")==null)
				   &&(target.fetchAffect("Spell_MassHaste")==null))
				{
					mob.location().send(mob,msg);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> speed(s) up!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and chant(s) quickly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}