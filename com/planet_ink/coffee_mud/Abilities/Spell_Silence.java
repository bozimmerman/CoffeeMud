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

public class Spell_Silence extends Spell
	implements AlterationDevotion
{
	public Spell_Silence()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Silence";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Silence spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		addQualifyingClass(new Mage().ID(),14);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Silence();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		room.show(invoker, null, Affect.VISUAL_ONLY, "The sounds here begin to return.");
		super.unInvoke();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask() |  Sense.CAN_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask() |  Sense.CAN_HEAR);
	}


	public boolean invoke(MOB mob, Vector commands)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		Environmental target = mob.location();

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> fizzles a spell.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			return false;
		}


		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, Affect.VISUAL_WNOISE, Affect.VISUAL_ONLY, Affect.VISUAL_ONLY, "<S-NAME> chant(s) and gesture(s) and silence falls like a blanket.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),0);
			}
		}
		else
			return beneficialFizzle(mob,null,"<S-NAME> chant(s) for silence, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
