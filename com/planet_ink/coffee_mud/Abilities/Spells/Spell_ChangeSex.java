package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ChangeSex extends Spell
{
	public String ID() { return "Spell_ChangeSex"; }
	public String name(){return "Change Sex";}
	public String displayText(){return "(Change Sex)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){return new Spell_ChangeSex();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		char gender='M';
		if(affectableStats.getStat(CharStats.GENDER)!='F')
			gender='F';
		affectableStats.setStat(CharStats.GENDER,(int)gender);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("You feel more like yourself again.");
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

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> sing(s) a spell to <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=beneficialAffect(mob,target,0);
					target.recoverCharStats();
					target.recoverEnvStats();
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> become(s) "+target.charStats().genderName()+"!");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> sing(s) a spell to <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}