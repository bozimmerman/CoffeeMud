package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_BreatheWater extends Chant
{
	public String ID() { return "Chant_BreatheWater"; }
	public String name(){ return "Breathe Water";}
	public String displayText(){return "(Breathe Water)";}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Chant_BreatheWater();}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(mob,null,"Your ability to breathe underwater fades.");
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
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already a water breather.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> attain(s) an aquatic aura!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");

		return success;
	}
}
