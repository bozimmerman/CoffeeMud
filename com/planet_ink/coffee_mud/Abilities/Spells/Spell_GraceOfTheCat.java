package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_GraceOfTheCat extends Spell
{
	public String ID() { return "Spell_GraceOfTheCat"; }
	public String name(){return "Grace Of The Cat";}
	public String displayText(){return "(Grace Of The Cat spell)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){ return new Spell_GraceOfTheCat();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		int increase = 4;
		if (affectableStats.getCurrentClass().baseClass().equals("Thief"))
			increase = 8;
		if (affectableStats.getCurrentClass().baseClass().equals("Bard"))
			increase = 8;
		if (affectableStats.getCurrentClass().baseClass().equals("Fighter"))
			increase = 6;
		if (affectableStats.getCurrentClass().baseClass().equals("Mage"))
			increase = 6;
		if (affectableStats.getCurrentClass().baseClass().equals("Cleric"))
			increase = 4;
		if (affectableStats.getCurrentClass().baseClass().equals("Druid"))
			increase = 4;
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY) + increase);
		if(affectableStats.getStat(CharStats.DEXTERITY)>25)affectableStats.setStat(CharStats.DEXTERITY,25);
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> speak(s) and gesture(s) to <T-NAMESELF>.^?");
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
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) gracefully to <T-NAMESELF>, but nothing more happens.");


		// return whether it worked
		return success;
	}
}
