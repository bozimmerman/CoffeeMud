package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Bind extends ThiefSkill
{
	public String ID() { return "Thief_Bind"; }
	public String name(){ return "Bind";}
	public String displayText(){ return "(Bound by "+ropeName+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"BIND"};
	public String[] triggerStrings(){return triggerStrings;}
	private int maxRange=0;
	public int maxRange(){return maxRange;}
	public int minRange(){return 0;}
	public long flags(){return Ability.FLAG_BINDING;}
	public int usageType(){return USAGE_MOVEMENT;}

	public int amountRemaining=500;
	public String ropeName="the ropes";

	public Environmental newInstance(){	return new Thief_Bind();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against "+ropeName+" binding <S-HIM-HER>."))
				{
					amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}


	public void setAffectedOne(Environmental E)
	{
		if(!(E instanceof Item))
			super.setAffectedOne(E);
		else
			ropeName=E.name();
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
			if(!mob.amDead())
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of "+ropeName+".");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!Sense.isSleeping(target))&&(!auto))
		{
			mob.tell(target.name()+" doesn't look willing to cooperate.");
			return false;
		}
		if((mob.isInCombat())&&(!auto))
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

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(auto) maxRange=10;
			String str=auto?"<T-NAME> become(s) bound by "+ropeName+".":"<S-NAME> bind(s) <T-NAME> with "+ropeName+".";
			FullMsg msg=new FullMsg(mob,target,this,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND|CMMsg.MASK_MALICIOUS,auto?"":str,str,str);
			if((target.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				target.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(auto)
					{
						maxRange=0;
						double prof=0.0;
						Ability A=mob.fetchAbility("Specialization_Ranged");
						if(A!=null) prof=Util.div(A.profficiency(),20);
						amountRemaining=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level())*((int)Math.round(5.0+prof));
					}
					else
						amountRemaining=adjustedLevel(mob)*25;
					if((target.location()==mob.location())||(auto))
						success=maliciousAffect(mob,target,Integer.MAX_VALUE-1000,-1);
				}
				if((mob.getVictim()==target)&&(!auto))
					mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bind <T-NAME> and fail(s).");


		// return whether it worked
		return success;
	}
}