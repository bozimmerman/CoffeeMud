package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Skill_HandCuff extends StdAbility
{
	public int amountRemaining=0;
	public int oldAssist=0;
	
	public Skill_HandCuff()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Handcuff";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Handcuffed)";

		canTargetCode=Ability.CAN_MOBS;
		canAffectCode=0;
		
		triggerStrings.addElement("HANDCUFF");
		triggerStrings.addElement("CUFF");
		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_HandCuff();
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
			if(affect.sourceMinor()==Affect.TYP_LEAVE)
				return true;
			else
			if((affect.sourceMinor()==Affect.TYP_ENTER)
			&&(affect.target()!=null)
			&&(affect.target() instanceof Room)
			&&(!((Room)affect.target()).isInhabitant(invoker)))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the ropes binding <S-HIM-HER>.");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(affect.sourceMinor()==Affect.TYP_ENTER)
				return true;
			else
			if((!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			&&((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOVE))))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against the ropes binding <S-HIM-HER>.");
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
		if(canBeUninvoked)
		{
			if(!mob.amDead())
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> released from the handcuffs.");
			if(oldAssist>0)
				mob.setBitmap(mob.getBitmap()|MOB.ATT_AUTOASSIST);
			ExternalPlay.standIfNecessary(mob);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		if((!Sense.isSleeping(target))&&(!Sense.isSitting(target)))
		{
			mob.tell(target.name()+" doesn't look willing to cooperate.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_NOISYMOVEMENT|Affect.MASK_MALICIOUS,"<S-NAME> handcuff(s) <T-NAME>.");
			if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					amountRemaining=adjustedLevel(mob)*200;
					if(target.location()==mob.location())
					{
						success=maliciousAffect(mob,target,Integer.MAX_VALUE-1000,-1);
						if(success)
						{
							oldAssist=target.getBitmap()&MOB.ATT_AUTOASSIST;
							if(oldAssist>0)
								target.setBitmap(target.getBitmap()-MOB.ATT_AUTOASSIST);
							target.setFollowing(mob);
						}
					}
				}
				if(mob.getVictim()==target) mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bind <T-NAME> and fail(s).");


		// return whether it worked
		return success;
	}
}