package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_GraceOfTheCat extends Spell
{
	public Spell_GraceOfTheCat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Grace Of The Cat";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Grace Of The Cat spell)";

		quality=Ability.BENEFICIAL_OTHERS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(6);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_GraceOfTheCat();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.SPELL_ALTERATION;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		int increase = 4;
		if (affectableStats.getMyClass().ID().equals("Thief"))
			increase = 8;
		if (affectableStats.getMyClass().ID().equals("Bard"))
			increase = 8;
		if (affectableStats.getMyClass().ID().equals("Paladin"))
			increase = 6;
		if (affectableStats.getMyClass().ID().equals("Ranger"))
			increase = 6;
		if (affectableStats.getMyClass().ID().equals("Fighter"))
			increase = 6;
		if (affectableStats.getMyClass().ID().equals("Mage"))
			increase = 6;
		if (affectableStats.getMyClass().ID().equals("Cleric"))
			increase = 4;
		affectableStats.setDexterity(affectableStats.getDexterity() + increase);
		if(affectableStats.getDexterity()>25)affectableStats.setDexterity(25);
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("You begin to feel more like your regular clumsy self.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		// now see if it worked
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> chant(s) and gesture(s) to <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> move(s) more gracefully!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) gracefully to <T-NAMESELF>, but nothing more happens.");


		// return whether it worked
		return success;
	}
}
