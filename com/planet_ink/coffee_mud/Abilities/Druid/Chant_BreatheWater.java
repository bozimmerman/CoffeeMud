package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_BreatheWater extends Chant
{
	public String ID() { return "Chant_BreatheWater"; }
	public String name(){ return "Fish Gills";}
	public String displayText(){return "(Fish Gills)";}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Chant_BreatheWater();}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your fish gills disappear.");
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affectableStats.getBodyPart(Race.BODY_GILL)==0)
			affectableStats.alterBodypart(Race.BODY_GILL,2);
		super.affectCharStats(affected,affectableStats);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((mob.location()!=null)
		&&((mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)||(mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)))
			if((mob.envStats().sensesMask()&EnvStats.CAN_NOT_BREATHE)==EnvStats.CAN_NOT_BREATHE)
				affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_NOT_BREATHE);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			target.tell("You are already a water breather.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> grow(s) a pair of gills!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");

		return success;
	}
}
