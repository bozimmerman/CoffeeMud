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

public class Spell_GraceOfTheCat extends Spell
	implements AlterationDevotion
{
	public Spell_GraceOfTheCat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Grace-Of-The-Cat";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Grace-Of-The-Cat spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(6);

		addQualifyingClass(new Mage().ID(),6);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_GraceOfTheCat();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		int increase = 4;
		if (affectableStats.getMyClass() instanceof Thief)
			increase = 8;
		if (affectableStats.getMyClass() instanceof Bard)
			increase = 8;
		if (affectableStats.getMyClass() instanceof Paladin)
			increase = 6;
		if (affectableStats.getMyClass() instanceof Ranger)
			increase = 6;
		if (affectableStats.getMyClass() instanceof Fighter)
			increase = 6;
		if (affectableStats.getMyClass() instanceof Mage)
			increase = 6;
		if (affectableStats.getMyClass() instanceof Cleric)
			increase = 4;
		affectableStats.setDexterity(affectableStats.getDexterity() + increase);
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("You begin to feel more like your regular clumsy self.");
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;


		// now see if it worked
		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) and gesture(s) to <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> move(s) more gracefully!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> chant(s) gracefully to <T-NAME>, but nothing more happens.");


		// return whether it worked
		return success;
	}
}
