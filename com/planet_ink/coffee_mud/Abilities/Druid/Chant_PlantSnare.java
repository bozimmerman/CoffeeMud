package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantSnare extends Chant
{

	public int amountRemaining=0;

	public Chant_PlantSnare()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Plant Snare";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Snared)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=0;
		maxRange=2;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_PlantSnare();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			&&((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOVE))))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the snaring plants.");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(!mob.amDead())
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the plants.");
		ExternalPlay.standIfNecessary(mob);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth snaring.");
			return false;
		}
		Room room=mob.location();
		if((room.domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_PLAINS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_HILLS)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(room.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP))
			mob.tell("There doesn't seem to be a large enough mass of plant life around here...\n\r");

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.location().show(mob,null,affectType,auto?"":"<S-NAME> chant(s) to the plants around <S-HIM-HER>.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,null);
				if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						amountRemaining=150;
						if(target.location()==mob.location())
						{
							success=maliciousAffect(mob,target,(adjustedLevel(mob)*10),-1);
							target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> become(s) stuck as tangling mass of plant life grows onto <S-HIM-HER>!");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fades.");


		// return whether it worked
		return success;
	}
}