package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_CharmArea extends Chant
{
	public String ID() { return "Chant_CharmArea"; }
	public String name(){ return "Calm Area";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_CharmArea();}
	
	public boolean okAffect(Affect affect)
	{
		if(affect.amITarget(affected)&&(affect.targetMinor()==Affect.TYP_LEAVE)
		   &&(!affect.amISource(invoker))
		   &&(affect.source().amFollowing()!=invoker))
		{
			affect.source().tell("You really don't feel like leaving this place.  It is just too beautiful.");
			return false;
		}
		return super.okAffect(affect);
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if(affect.amITarget(affected)&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
		{
			affect.addTrailerMsg(new FullMsg(affect.source(),null,null,Affect.MSG_OK_VISUAL,Affect.NO_EFFECT,Affect.NO_EFFECT,"There is something charming about this place."));
		}
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;

		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY))&&(!auto))
		{
			mob.tell("This chant does not work here.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"This area seems to twinkle with beauty.":"^S<S-NAME> chant(s), bringing forth the natural beauty of this place.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but the magic fades.");

		// return whether it worked
		return success;
	}
}
