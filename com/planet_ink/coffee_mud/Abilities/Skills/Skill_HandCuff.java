package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
	public int usageType(){return USAGE_MOVEMENT;}

	public int amountRemaining=0;
	public boolean oldAssist=false;
	public boolean oldGuard=false;


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
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				if((msg.source()!=null)&&(msg.source().location()!=null))
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the handcuffs prevent <S-HIM-HER>.");
				return false;
			}
			else
			if(((msg.sourceMinor()==CMMsg.TYP_FOLLOW)&&(msg.target()!=invoker()))
			||((msg.sourceMinor()==CMMsg.TYP_NOFOLLOW)&&(msg.source().amFollowing()==invoker())))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				return true;
			else
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target()!=null)
			&&(msg.target() instanceof Room)
			&&(!((Room)msg.target()).isInhabitant(invoker))))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)+mob.envStats().level());
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
			else
			if(msg.sourceMinor()==CMMsg.TYP_ENTER)
				return true;
			else
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) against <S-HIS-HER> cuffs.");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		else
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&(msg.amITarget(affected))
		&&(!mob.isInCombat())
		&&(mob.amFollowing()!=null)
		&&(msg.source().isMonster())
		&&(msg.source().getVictim()!=mob))
		{
			msg.source().tell("You may not assault this prisoner.");
			if(mob.getVictim()==msg.source())
			{
				mob.makePeace();
				mob.setVictim(null);
			}
			return false;
		}
		return super.okMessage(myHost,msg);
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
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> released from the handcuffs.");
			if(!oldAssist)
				mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOASSIST));
			if(oldGuard)
				mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_AUTOGUARD));
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(!auto))
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
	    if((commands.size()>0)&&((String)commands.firstElement()).equalsIgnoreCase("UNTIE"))
	    {
	        commands.removeElementAt(0);
			MOB target=super.getTarget(mob,commands,givenTarget,false,true);
			if(target==null) return false;
			Ability A=target.fetchEffect(ID());
			if(A!=null)
			{
			    if(mob.location().show(mob,target,null,CMMsg.MSG_HANDS,"<S-NAME> attempt(s) to unbind <T-NAMESELF>."))
			    {
				    A.unInvoke();
				    return true;
			    }
		        return false;
			}
		    mob.tell(target.name()+" doesn't appear to be handcuffed.");
		    return false;
	    }
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(Skill_Arrest.getWarrantsOf(target, CoffeeUtensils.getLegalObject(mob.location().getArea())).size()==0)
		{
		    mob.tell(target.name()+" has no warrants out here.");
		    return false;
		}
		if((Sense.isStanding(target))&&(!auto))
		{
			mob.tell(target.name()+" doesn't look willing to cooperate.");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_GENERAL:CMMsg.MASK_MALICIOUS),"<S-NAME> handcuff(s) <T-NAME>.");
			if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					amountRemaining=adjustedLevel(mob,asLevel)*300;
					if(target.location()==mob.location())
					{
						success=maliciousAffect(mob,target,asLevel,Integer.MAX_VALUE-1000,-1);
						if(success)
						{
							oldAssist=Util.bset(target.getBitmap(),MOB.ATT_AUTOASSIST);
							if(!oldAssist)
								target.setBitmap(Util.setb(target.getBitmap(),MOB.ATT_AUTOASSIST));
							oldGuard=Util.bset(target.getBitmap(),MOB.ATT_AUTOASSIST);
							if(oldGuard)
								target.setBitmap(Util.unsetb(target.getBitmap(),MOB.ATT_AUTOGUARD));
							boolean oldNOFOL=Util.bset(target.getBitmap(),MOB.ATT_NOFOLLOW);
							if(target.numFollowers()>0)
								CommonMsgs.doStandardCommand(target,"NoFollow",Util.makeVector("UNFOLLOW","QUIETLY"));
							target.setBitmap(Util.unsetb(target.getBitmap(),MOB.ATT_NOFOLLOW));
							CommonMsgs.follow(target,mob,true);
							if(oldNOFOL)
								target.setBitmap(Util.setb(target.getBitmap(),MOB.ATT_NOFOLLOW));
							else
								target.setBitmap(Util.unsetb(target.getBitmap(),MOB.ATT_NOFOLLOW));
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
