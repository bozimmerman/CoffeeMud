package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_LightSensitivity extends Spell
{
	public String ID() { return "Spell_LightSensitivity"; }
	public String name(){return "Light Sensitivity";}
	public String displayText(){return "(Light Sensitivity)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_LightSensitivity();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!(affected instanceof MOB)) return;
		if(((MOB)affected).location()==null) return;
		if(Sense.isInDark(((MOB)affected).location()))
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
		else
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your light sensitivity returns to normal.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!auto)&&(target.charStats().getBodyPart(Race.BODY_EYE)==0))
		{
			mob.tell(target.name()+" has no eyes, and would not be affected.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			String autoStr="A flashing light blazes in the eyes of <T-NAME>!";
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?autoStr:"^SYou invoke a sensitive light into <T-NAME>s eyes.^?",affectType(auto),auto?autoStr:"^S<S-NAME> invoke(s) a sensitive light into your eyes.^?",CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL,auto?autoStr:"^S<S-NAME> invokes a sensitive light into <T-NAME>s eyes.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(Sense.isInDark(mob.location()))
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) extremely sensitive to light.");
					else
						mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> become(s) blinded by the light.");
					if(quality()==Ability.MALICIOUS)
						success=maliciousAffect(mob,target,0,-1);
					else
						success=beneficialAffect(mob,target,0);
				}
			}
		}
		else
		if(quality()==Ability.MALICIOUS)
			return maliciousFizzle(mob,target,"<S-NAME> invoke(s) at <T-NAMESELF>, but the spell fizzles.");
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> invoke(s) at <T-NAMESELF>, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}