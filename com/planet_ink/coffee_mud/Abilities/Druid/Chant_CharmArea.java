package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_CharmArea extends Chant
{
	public String ID() { return "Chant_CharmArea"; }
	public String name(){ return "Charm Area";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_CharmArea();}
	
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(affected)&&(affect.targetMinor()==Affect.TYP_LEAVE)
		   &&(!affect.amISource(invoker))
		   &&(affect.source().amFollowing()!=invoker))
		{
			affect.source().tell("You really don't feel like leaving this place.  It is just too beautiful.");
			return false;
		}
		if((Util.bset(affect.sourceCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.targetCode(),affect.MASK_MALICIOUS))
		||(Util.bset(affect.othersCode(),affect.MASK_MALICIOUS)))
		{
			if((affect.source()!=null)
			   &&(affect.target()!=null)
			   &&(affect.source()!=affect.target()))
			{
				affect.source().tell("Nah, you feel too peaceful here.");
				if(affect.source().getVictim()!=null)
					affect.source().getVictim().makePeace();
				affect.source().makePeace();
			}
			affect.modify(affect.source(),affect.target(),affect.tool(),Affect.NO_EFFECT,"",Affect.NO_EFFECT,"",Affect.NO_EFFECT,"");
			return false;
		}
		return super.okAffect(myHost,affect);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
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
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		&&(!auto))
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
			if(mob.location().okAffect(mob,msg))
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
