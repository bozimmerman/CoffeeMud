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

public class Spell_GiantStrength extends Spell
	implements AlterationDevotion
{
	public Spell_GiantStrength()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Giant-Strength";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Giant-Strength spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Mage().ID(),13);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_GiantStrength();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		int increase = 4;
		if (affectableStats.getMyClass() instanceof Fighter)
			increase = 8;
		if (affectableStats.getMyClass() instanceof Ranger)
			increase = 8;
		if (affectableStats.getMyClass() instanceof Paladin)
			increase = 8;
		if (affectableStats.getMyClass() instanceof Mage)
			increase = 5;
		if (affectableStats.getMyClass() instanceof Thief)
			increase = 7;
		if (affectableStats.getMyClass() instanceof Bard)
			increase = 7;
		if (affectableStats.getMyClass() instanceof Cleric)
			increase = 6;
		affectableStats.setStrength(affectableStats.getStrength() + increase);
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("Your muscles shrink back to their normal size.");
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) to <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> grow(s) huge muscles!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> chant(s) really big to <T-NAME>, but nothing more happens.");


		// return whether it worked
		return success;
	}
}
