package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Ensnare extends Spell
{

	public int amountRemaining=0;

	public Spell_Ensnare()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Ensnare";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Ensnared)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(15);

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=1;
		maxRange=2;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Ensnare();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
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
			switch(affect.sourceMinor())
			{
			case Affect.TYP_ENTER:
			case Affect.TYP_ADVANCE:
			case Affect.TYP_LEAVE:
			case Affect.TYP_FLEE:
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the ensnarement.");
				amountRemaining-=mob.envStats().level();
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okAffect(affect);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition((int)(affectableStats.disposition()&(EnvStats.ALLMASK-EnvStats.IS_FLYING)));
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the ensnarement.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth ensnaring.");
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
			mob.location().show(mob,null,affectType,auto?"":"<S-NAME> chant(s) and wave(s) <S-HIS-HER> fingers at the ground.");
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
						amountRemaining=60;
						if(target.location()==mob.location())
						{
							success=maliciousAffect(mob,target,0,-1);
							target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> become(s) ensnared, and is unable to move <S-HIS-HER> feet!");
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s) and wave(s) <S-HIS-HER> fingers, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}