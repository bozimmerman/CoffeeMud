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

public class Spell_Blur extends Spell
	implements IllusionistDevotion
{
	public Spell_Blur()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Blur";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Blur spell)";


		baseEnvStats().setLevel(2);

		addQualifyingClass(new Mage().ID(),2);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Blur();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob))
		{
			switch(affect.targetCode())
			{
			case Affect.STRIKE_MAGIC:
				if(invoker()!=null)
				{
					int pctDodge=mob.charStats().getIntelligence();
					if(Dice.rollPercentage()<pctDodge)
					{
						FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<T-NAME> can't seem to focus on <S-NAME>.");
						mob.location().send(mob,msg);
						return false;
					}
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor() - 5);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		mob.tell("You begin to come back into focus.");
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

		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a spell.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> look(s) blurry!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
