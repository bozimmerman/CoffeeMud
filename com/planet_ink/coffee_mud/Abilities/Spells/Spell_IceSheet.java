package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_IceSheet extends Spell
{
	public String ID() { return "Spell_IceSheet"; }
	public String name(){return "Ice Sheet";}
	public String displayText(){return "(Ice Sheet spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){ return new Spell_IceSheet();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if(canBeUninvoked)
			room.showHappens(Affect.MSG_OK_VISUAL, "The ice sheet melts.");
		super.unInvoke();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof Room)))
		   return false;
		Room room=(Room)affected;
		if(affect.source().location()==room)
		{
			MOB mob=affect.source();
			if(!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			{
				if((room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
				||(room.domainType()==Room.DOMAIN_INDOORS_UNDERWATER))
				{
					mob.tell("You are frozen in the ice sheet and can't even blink.");
					return false;
				}
				else
				if((Util.bset(affect.sourceMajor(),Affect.ACT_MOVE)))
				{
					if((!Sense.isFlying(mob))
					&&(Dice.rollPercentage()>((affect.source().charStats().getStat(CharStats.DEXTERITY)*3)+25)))
					{
						int oldDisposition=mob.baseEnvStats().disposition();
						oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
						mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
						mob.recoverEnvStats();
						mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> slip(s) on the ice.");
						return false;
					}
				}
			}
		}
		return super.okAffect(affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// sleeping for a room disables any special characteristic (as of water)
		if(affected instanceof Room)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"Ice Sheet has already been cast here!");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			return false;
		}


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			String msgStr="the ground becomes covered in ice!";
			if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			||(mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE))
				msgStr="the water freezes over!";
			if(auto)msgStr=Character.toUpperCase(msgStr.charAt(0))+msgStr.substring(1);
			FullMsg msg = new FullMsg(mob, target, this, affectType(auto),(auto?"":"^S<S-NAME> speak(s) and gesture(s) and ")+msgStr+"^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> speak(s) about darkness, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
