package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_CharmAnimal extends Chant
{
	public Chant_CharmAnimal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Charm Animal";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Charmed)";

		quality=Ability.MALICIOUS;


		canAffectCode=0;
		canTargetCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(6);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_CharmAnimal();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.source()==mob.amFollowing()))
				unInvoke();
		if((affect.amISource(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.target()==mob.amFollowing()))
		{
			mob.tell("You like "+mob.amFollowing().charStats().himher()+" too much.");
			return false;
		}

		return super.okAffect(affect);
	}

	public boolean tick(int tickID)
	{
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return false;
		MOB mob=(MOB)affecting();
		if((affected==mob)&&((mob.amFollowing()==null)||(mob.amFollowing()!=invoker)))
			ExternalPlay.follow(mob,invoker,true);
		return super.tick(tickID);
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your free-will returns.");
		ExternalPlay.follow(mob,null,false);
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.charStats().getStat(CharStats.INTELLIGENCE)>1)
		{
			mob.tell(target.name()+" is not an animal!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str=auto?"":"<S-NAME> chant(s) at <T-NAMESELF>";
			FullMsg msg=new FullMsg(mob,target,this,affectType,str+".");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,Affect.MSK_CAST_VERBAL|Affect.TYP_MIND);
					if(success)
					{
						ExternalPlay.follow(target,mob,false);
						ExternalPlay.makePeaceInGroup(mob);
					}
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
