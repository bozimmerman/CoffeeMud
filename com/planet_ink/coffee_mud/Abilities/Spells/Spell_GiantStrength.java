package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_GiantStrength extends Spell
{
	public String ID() { return "Spell_GiantStrength"; }
	public String name(){return "Giant Strength";}
	public String displayText(){return "(Giant-Strength spell)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){ return new Spell_GiantStrength();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		int increase = 4;
		if (affectableStats.getMyClass().ID().equals("Fighter"))
			increase = 8;
		else
		if (affectableStats.getMyClass().ID().equals("Ranger"))
			increase = 8;
		else
		if (affectableStats.getMyClass().ID().equals("Paladin"))
			increase = 8;
		else
		if (affectableStats.getMyClass().ID().equals("Mage"))
			increase = 5;
		else
		if (affectableStats.getMyClass().ID().equals("Thief"))
			increase = 7;
		else
		if (affectableStats.getMyClass().ID().equals("Bard"))
			increase = 7;
		else
		if (affectableStats.getMyClass().ID().equals("Cleric"))
			increase = 6;
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH) + increase);
		if(affectableStats.getStat(CharStats.STRENGTH)>25)affectableStats.setStat(CharStats.STRENGTH,25);
	}


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
			mob.tell("Your muscles shrink back to their normal size.");
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
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> grow(s) huge muscles!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) strongly to <T-NAMESELF>, but nothing more happens.");


		// return whether it worked
		return success;
	}
}
