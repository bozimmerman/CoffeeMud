package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MassFeatherfall extends Spell
{
	public String ID() { return "Spell_MassFeatherfall"; }
	public String name(){return "Mass FeatherFall";}
	public String displayText(){return "";}
	public int quality(){ return OK_OTHERS;}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Spell_MassFeatherfall();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=properTargets(mob,givenTarget,false);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth floating.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) lightly.^?"))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					Spell_FeatherFall fall=new Spell_FeatherFall();
					fall.setProfficiency(profficiency());
					fall.beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) lightly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}