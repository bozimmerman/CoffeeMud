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

public class Spell_MassInvisibility extends Spell
	implements AlterationDevotion
{
	public Spell_MassInvisibility()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mass Invisibility";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Invisibility)";


		malicious=true;

		baseEnvStats().setLevel(14);

		addQualifyingClass(new Mage().ID(),14);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MassInvisibility();
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			switch(affect.sourceType())
			{
			case Affect.STRIKE:
				unInvoke();
				break;
			default:
				break;
			}
		}
		return;
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_INVISIBLE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("You become visible again.");
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		Hashtable h=Grouping.getAllFollowers(mob);
		if(h.get(mob.ID())==null) h.put(mob.ID(),mob);

		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth making invisible.");
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
			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> wave(s) <S-HIS-HER> arms and chant(s) softly.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.VISUAL_WNOISE,null);
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> fade(s) from view!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and chant(s) softly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}