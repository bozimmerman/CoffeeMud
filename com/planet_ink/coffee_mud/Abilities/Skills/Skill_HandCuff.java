package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Skill_HandCuff extends StdAbility
{
	public String ID() { return "Skill_HandCuff"; }
	public String name(){ return "Handcuff";}
	public String displayText(){ return "(Handcuffed)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"HANDCUFF","CUFF"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public long flags(){return Ability.FLAG_BINDING;}

	public int amountRemaining=0;
	public boolean oldAssist=false;

	public Environmental newInstance(){	return new Skill_HandCuff();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if(affect.sourceMinor()==affect.TYP_RECALL)
			{
				if((affect.source()!=null)&&(affect.source().location()!=null))
					affect.source().location().show(affect.source(),null,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the handcuffs prevent <S-HIM-HER>.");
				return false;
			}
			else
			if(((affect.sourceMinor()==Affect.TYP_FOLLOW)&&(affect.target()!=invoker()))
			||((affect.sourceMinor()==Affect.TYP_NOFOLLOW)&&(affect.source().amFollowing()==invoker())))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(affect.sourceMinor()==Affect.TYP_LEAVE)
				return true;
			else
			if(((affect.sourceMinor()==Affect.TYP_ENTER)
			&&(affect.target()!=null)
			&&(affect.target() instanceof Room)
			&&(!((Room)affect.target()).isInhabitant(invoker))))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(affect.sourceMinor()==Affect.TYP_ENTER)
				return true;
			else
			if((!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
			&&((Util.bset(affect.sourceMajor(),Affect.MASK_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.MASK_MOVE))))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		else
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&(affect.amITarget(affected))
		&&(!mob.isInCombat())
		&&(mob.amFollowing()!=null)
		&&(affect.source().isMonster())
		&&(affect.source().getVictim()!=mob))
		{
			affect.source().tell("You may not assault this prisoner.");
			if(mob.getVictim()==affect.source())
			{
				mob.makePeace();
				mob.setVictim(null);
			}
			return false;
		}
		return super.okAffect(myHost,affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.setFollowing(null);
			if(!mob.amDead())
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> released from the handcuffs.");
			if(!oldAssist)
				mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOASSIST));
			ExternalPlay.standIfNecessary(mob);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!Sense.isSleeping(target))&&(!Sense.isSitting(target))&&(!auto))
		{
			mob.tell(target.name()+" doesn't look willing to cooperate.");
			return false;
		}
		if(mob.isInCombat()&&(!auto))
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
			if((mob.location().okAffect(mob,msg))&&(target.fetchAffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					amountRemaining=adjustedLevel(mob)*300;
					if(target.location()==mob.location())
					{
						success=maliciousAffect(mob,target,Integer.MAX_VALUE-1000,-1);
						if(success)
						{
							oldAssist=Util.bset(target.getBitmap(),MOB.ATT_AUTOASSIST);
							if(!oldAssist)
								target.setBitmap(Util.setb(target.getBitmap(),MOB.ATT_AUTOASSIST));
							ExternalPlay.unfollow(target,true);
							ExternalPlay.follow(target,mob,true);
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