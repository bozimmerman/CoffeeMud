package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtectHealth extends Prayer
{
	public String ID() { return "Prayer_ProtectHealth"; }
	public String name(){ return "Protect Health";}
	public int quality(){ return BENEFICIAL_SELF;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	public String displayText(){ return "(Protection of Mind and Body)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_ProtectHealth();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("Your bodies natural defenses take over.");
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+50);
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if(affect.target()==invoker)
		{
			if((affect.tool()!=null)
			   &&(Dice.rollPercentage()>50)
			   &&((affect.tool().text().equalsIgnoreCase("DISEASE"))
				||(affect.targetMinor()==Affect.TYP_DISEASE)))
			{
				affect.source().location().show(invoker,null,Affect.MSG_OK_VISUAL,"An unhealthy assault against <S-NAME> is magically repelled.");
				return false;
			}

		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(target==null) return false;
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("You already have protected health.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) a healthy mind and body.":"^S<S-NAME> pray(s) for a healthy mind and body.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for a healthy body and mind, but nothing happens.");


		// return whether it worked
		return success;
	}
}
