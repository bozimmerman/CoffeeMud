package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Philosophy extends Prayer
{
	public String ID() { return "Prayer_Philosophy"; }
	public String name(){return "Philosophy";}
	public String displayText(){return "(Philosophy spell)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){ return new Prayer_Philosophy();}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		int increase = 4;
		if (affectableStats.getCurrentClass().baseClass().equals("Fighter"))
			increase = 4;
		else
		if (affectableStats.getCurrentClass().baseClass().equals("Mage"))
			increase = 6;
		else
		if (affectableStats.getCurrentClass().baseClass().equals("Thief"))
			increase = 5;
		else
		if (affectableStats.getCurrentClass().baseClass().equals("Bard"))
			increase = 5;
		else
		if (affectableStats.getCurrentClass().baseClass().equals("Cleric"))
			increase = 7;
		else
		if (affectableStats.getCurrentClass().baseClass().equals("Druid"))
			increase = 7;
		affectableStats.setStat(CharStats.WISDOM,affectableStats.getStat(CharStats.WISDOM) + increase);
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You stop pondering life and mysteries of the universe.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> give(s) <T-NAMESELF> something to think about.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> start(s) pondering the mysteries of the universe.");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> give(s) <T-NAMESELF> something to think about, but just confuses <T-HIMHER>.");


		// return whether it worked
		return success;
	}
}
