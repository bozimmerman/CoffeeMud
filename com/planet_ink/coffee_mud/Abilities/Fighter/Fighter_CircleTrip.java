package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Fighter_CircleTrip extends StdAbility
{
	boolean doneTicking=false;
	public String ID() { return "Fighter_CircleTrip"; }
	public String name(){ return "Circle Trip";}
	public String displayText(){ return "(Tripped)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"CIRCLETRIP","CTRIP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		else
		if(msg.amISource(mob)&&(msg.sourceMinor()==CMMsg.TYP_STAND))
			return false;
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> regain(s) <S-HIS-HER> feet.");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					CommonMsgs.stand(mob,true);
				}
			}
			else
				mob.tell("You regain your feet.");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((!Sense.aliveAwakeMobile(mob,true)||(Sense.isSitting(mob))))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to circle trip!");
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_LEG)<=1)
		{
			mob.tell("You need at least two legs to do this.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth tripping.");
			return false;
		}

		boolean success=true;
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),auto?"":"^F<S-NAME> slide(s) into a circle trip!^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(Iterator e=h.iterator();e.hasNext();)
			{
				MOB target=(MOB)e.next();

				if((Sense.isSitting(target)||Sense.isSleeping(target)))
				{
					mob.tell(target.name()+" is already on the floor!");
					return false;
				}

				if(target.riding()!=null)
				{
					mob.tell("You can't trip someone "+target.riding().stateString(target)+" "+target.riding().name()+"!");
					return false;
				}
				if(Sense.isInFlight(target))
				{
					mob.tell(target.name()+" is flying and can't be tripped!");
					return false;
				}

				int levelDiff=target.envStats().level()-mob.envStats().level();
				if(levelDiff>0)
					levelDiff=levelDiff*5;
				else
					levelDiff=0;
				int adjustment=(-levelDiff)+(-(35+((int)Math.round((new Integer(target.charStats().getStat(CharStats.DEXTERITY)).doubleValue()-9.0)*3.0))));
				success=profficiencyCheck(mob,adjustment,auto);
				success=success&&(target.charStats().getBodyPart(Race.BODY_LEG)>0);
				if(success)
				{
					msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),auto?"<T-NAME> trip(s)!":"^F<S-NAME> trip(s) <T-NAMESELF>!^?");
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						maliciousAffect(mob,target,2,-1);
						target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the floor!");
					}
				}
				else
					return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to circle trip <T-NAMESELF>, but fail(s).");
			}
		}
		else
			success=false;
		return success;
	}
}
