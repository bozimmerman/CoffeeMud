package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Silence extends Spell
{
	public Spell_Silence()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Silence";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Silence spell)";

		canAffectCode=Ability.CAN_ROOMS;
		canTargetCode=0;


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Silence();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}



	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if(canBeUninvoked)
			room.showHappens(Affect.MSG_OK_ACTION, "The sounds here begin to return.");
		super.unInvoke();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask() |  EnvStats.CAN_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask() |  EnvStats.CAN_HEAR);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> fizzles a spell.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			return false;
		}


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType,(auto?"S":"^S<S-NAME> whisper(s) and gesture(s) and s")+"ilence falls like a blanket.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> whisper(s) about silence, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
