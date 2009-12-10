package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Thief_Shadow extends ThiefSkill
{
	public String ID() { return "Thief_Shadow"; }
	public String name(){ return "Shadow";}
		// can NOT have a display text since the ability instance
		// is shared between the invoker and the target
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;}
	private static final String[] triggerStrings = {"SHADOW"};
	public String[] triggerStrings(){return triggerStrings;}
	public MOB shadowing=null;
	protected Room lastRoom=null;
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
		if(!CMLib.flags().aliveAwakeMobile(mob,true)) return false;
		return true;
	}

	public boolean stillAShadowee()
	{
		if(shadowing==null) return false;
		if(shadowing.amDead()) return false;
		if(shadowing.isInCombat()&&(shadowing.getVictim()==invoker)) return false;
		if(shadowing.location()==null) return false;
		if(!CMLib.flags().aliveAwakeMobile(shadowing,true)) return false;
		return true;
	}

	public boolean canShadow()
	{
		if(!stillAShadower()) return false;
		if(!stillAShadowee()) return false;
		MOB mob=invoker;
		if(CMLib.flags().canBeSeenBy(mob,shadowing)) return false;
		if(!CMLib.flags().canBeSeenBy(shadowing,mob)) return false;
		if(mob.location()!=shadowing.location()) return false;
		if(mob.getGroupMembers(new HashSet()).size()>1) return false;
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
		 ||(msg.targetMinor()==CMMsg.TYP_FLEE))
		&&(stillAShadower())
		&&(stillAShadowee())
		&&(msg.amISource(shadowing))
		&&(msg.amITarget(shadowing.location()))
		&&(!CMLib.flags().isSneaking(shadowing))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Exit)
        &&((shadowing.riding()==null)||(msg.source().riding()!=shadowing.riding())))
		{
			int dir=-1;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(shadowing.location().getReverseExit(d)==msg.tool())
					dir=d;
			if((dir>=0)&&(msg.source().location()!=lastRoom))
			{
				String directionWent=Directions.getDirectionName(dir);
				MOB mob=invoker;
				lastRoom=msg.source().location();
				if(!mob.isMonster())
					mob.enqueCommand(CMParms.parse(directionWent),Command.METAFLAG_FORCED,0);
				else
					CMLib.tracking().move(mob,dir,false,false);
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

    public void affectCharStats(MOB affected, CharStats affectableStats)
    {
        super.affectCharStats(affected,affectableStats);
        if((shadowing!=null)&&(shadowing.location()==affected.location()))
            affectableStats.setStat(CharStats.STAT_SAVE_DETECTION,
                    25
                    +proficiency()
                    +affectableStats.getStat(CharStats.STAT_SAVE_DETECTION));
        else
            affectableStats.setStat(CharStats.STAT_SAVE_DETECTION,
                    +proficiency()
                    +affectableStats.getStat(CharStats.STAT_SAVE_DETECTION));
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
		if(CMLib.flags().canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		shadowing=null;
		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(super.getXLEVELLevel(mob)*2));

		boolean success=proficiencyCheck(mob,-(levelDiff*10),auto);

		if(!success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,auto?"":"Your attempt to shadow <T-NAMESELF> fails; <T-NAME> spots you!",CMMsg.MSG_OK_VISUAL,auto?"":"You spot <S-NAME> trying to shadow you.",CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_THIEF_ACT,"You are now shadowing <T-NAME>.  Enter 'shadow' again to disengage.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
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
					{
						A=(Thief_Shadow)mob.fetchEffect(ID());
						if(A!=null)
							A.unInvoke();
					}
				}
			}
		}
		return success;
	}
}
