package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Thief_Shadow extends ThiefSkill
{
	public String ID() { return "Thief_Shadow"; }
	public String name(){ return "Shadow";}
	public String displayText()
	{
		if(shadowing!=null)
			return "(shadowing "+shadowing.name()+")";
		return super.displayText();
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"SHADOW"};
	public String[] triggerStrings(){return triggerStrings;}
	public MOB shadowing=null;
	private Room lastRoom=null;
	private long lastTogether=0;
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean stillAShadower()
	{
		if(invoker==null) return false;
		MOB mob=invoker;
		if(mob.amDead()) return false;
		if(mob.isInCombat()) return false;
		if(mob.location()==null) return false;
		if(!Sense.aliveAwakeMobile(mob,true)) return false;
		return true;
	}

	public boolean stillAShadowee()
	{
		if(shadowing==null) return false;
		if(shadowing.amDead()) return false;
		if(shadowing.isInCombat()&&(shadowing.getVictim()==invoker)) return false;
		if(shadowing.location()==null) return false;
		if(!Sense.aliveAwakeMobile(shadowing,true)) return false;
		return true;
	}

	public boolean canShadow()
	{
		if(!stillAShadower()) return false;
		if(!stillAShadowee()) return false;
		MOB mob=invoker;
		if(Sense.canBeSeenBy(mob,shadowing)) return false;
		if(!Sense.canBeSeenBy(shadowing,mob)) return false;
		if(mob.location()!=shadowing.location()) return false;
		if(mob.getGroupMembers(new HashSet()).size()>1) return false;
		return true;
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
		 ||(msg.targetMinor()==CMMsg.TYP_FLEE))
		&&(stillAShadower())
		&&(stillAShadowee())
		&&(msg.amISource(shadowing))
		&&(msg.amITarget(shadowing.location()))
		&&(!Sense.isSneaking(shadowing))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Exit))
		{

			int dir=-1;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(shadowing.location().getReverseExit(d)==msg.tool())
					dir=d;
			if((dir>=0)&&(msg.source().location()!=lastRoom))
			{
				String directionWent=Directions.getDirectionName(dir);
				MOB mob=invoker;
				lastRoom=msg.source().location();
				if(!mob.isMonster())
					mob.enqueCommand(Util.parse(directionWent),0);
				else
					MUDTracker.move(mob,dir,false,false);
			}
		}
		if((shadowing!=null)&&(invoker!=null)&&(shadowing.location()==invoker.location()))
			lastTogether=System.currentTimeMillis();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(lastTogether==0) return true;
		if((shadowing!=null)&&(invoker!=null)&&(shadowing.location()==invoker.location()))
			lastTogether=System.currentTimeMillis();
		long secondsago=System.currentTimeMillis()-10000;
		if(lastTogether<secondsago)
		{
			if((invoker!=null)&&(shadowing!=null))
			{
				invoker.tell("You lost "+shadowing.charStats().himher()+".");
				unInvoke();
				return false;
			}
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==invoker)
		{
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
		}
		if((shadowing!=null)&&(invoker!=null)&&(shadowing.location()==invoker.location()))
			lastTogether=System.currentTimeMillis();
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((invoker!=null)&&(shadowing!=null))
			{
				invoker.delEffect(this);
				setAffectedOne(shadowing);
				invoker.tell("You are no longer shadowing "+shadowing.name()+".");
			}
			shadowing=null;
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Thief_Shadow A=(Thief_Shadow)mob.fetchEffect(ID());
		if(A!=null)
		{
			if(A.shadowing==null)
				mob.delEffect(A);
			else
			{
				Ability AA=A.shadowing.fetchEffect(ID());
				if((AA!=null)&&(AA.invoker()==mob))
				{
					AA.unInvoke();
					return true;
				}
				else
					mob.delEffect(A);
			}
		}
		if(commands.size()<1)
		{
			mob.tell("Shadow whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("You cannot shadow yourself?!");
			return false;
		}
		if(mob.getGroupMembers(new HashSet()).size()>1)
		{
			mob.tell("You cannot shadow someone while part of a group.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
		if(Sense.canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		shadowing=null;
		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());

		boolean success=profficiencyCheck(mob,-(levelDiff*10),auto);

		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,auto?"":"Your attempt to shadow <T-NAMESELF> fails; <T-NAME> spots you!",CMMsg.MSG_OK_VISUAL,auto?"":"You spot <S-NAME> trying to shadow you.",CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_THIEF_ACT,"You are now shadowing <T-NAME>.  Enter 'shadow' again to disengage.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				shadowing=target;
				if(beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE-1000))
				{
					A=(Thief_Shadow)target.fetchEffect(ID());
					if(A!=null)
					{
						mob.addEffect(A);
						A.shadowing=target;
						A.setAffectedOne(target);
						A.lastTogether=System.currentTimeMillis();
						mob.recoverEnvStats();
					}
					else
						A.unInvoke();
				}
			}
		}
		return success;
	}
}
