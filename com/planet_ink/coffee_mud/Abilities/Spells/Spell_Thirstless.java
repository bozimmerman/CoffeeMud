package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Thirstless extends Spell
{
	public String ID() { return "Spell_Thirstless"; }
	public String name(){return "Thirstless";}
	public String displayText(){return "(Thirstless)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Thirstless();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void affectCharState(MOB affected, CharState affectableMaxState)
	{
		super.affectCharState(affected,affectableMaxState);
		affectableMaxState.setThirst(999999);
		affected.curState().setThirst(affectableMaxState.getThirst());
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			mob.tell("You are starting to feel thirstier.");
			mob.curState().setThirst(0);
		}
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> cast(s) a spell on <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				mob.tell("You feel quenched!");
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) thirstily to <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}