package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_FireWard extends Chant
{
	public String ID() { return "Chant_FireWard"; }
	public String name(){ return "Fire Ward";}
	public String displayText(){return "(Fire Ward)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public Environmental newInstance(){	return new Chant_FireWard();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your cool protection warms up.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_FIRE,affectedStats.getStat(CharStats.SAVE_FIRE)+100);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already warding fire.");
			return false;
		}
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
			target=(MOB)givenTarget;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> chant(s) for a cool field of protection around <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for fire protection, but fail(s).");

		return success;
	}
}