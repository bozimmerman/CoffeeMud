package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_GustOfWind extends Spell
{
	public boolean doneTicking=false;
	public Spell_GustOfWind()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Gust of Wind";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Blown Down)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=0;
		maxRange=4;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_GustOfWind();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_EVOCATION;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((doneTicking)&&(affect.amISource(mob)))
			unInvoke();
		else
		if(affect.amISource(mob)&&(affect.sourceMinor()==Affect.TYP_STAND))
			return false;
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		doneTicking=true;
		super.unInvoke();
		if(!mob.amDead())
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> regain(s) <S-HIS-HER> feet.");
			else
				mob.tell("You regain your feet.");
			ExternalPlay.standIfNecessary(mob);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell("There doesn't appear to be anyone here worth blowing around.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			mob.location().show(mob,null,affectType,auto?"A horrendous wind gust blows through here.":"<S-NAME> blow(s) at <S-HIS-HER> enemies.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType,"<T-NAME> get(s) blown back!");
				if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
				{
					if((!msg.wasModified())&&(target.location()==mob.location()))
					{
						MOB victim=target.getVictim();
						if((victim!=null)&&(target.rangeToTarget()>=0))
							target.setAtRange(target.rangeToTarget()+1);
						mob.location().send(mob,msg);
						if(Dice.rollPercentage()>((target.charStats().getStat(CharStats.DEXTERITY)*2)+target.envStats().level()))
						{
							mob.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> fall(s) down!");
							doneTicking=false;
							success=maliciousAffect(mob,target,2,-1);
						}
						if(target.getVictim()!=null)
							target.getVictim().setAtRange(target.rangeToTarget());
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> blow(s), but find(s) <S-HE-SHE> is only full of hot air.");


		// return whether it worked
		return success;
	}
}