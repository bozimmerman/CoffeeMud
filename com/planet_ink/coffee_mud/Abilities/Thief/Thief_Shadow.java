package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
	public Environmental newInstance(){	return new Thief_Shadow();}

	public boolean stillAShadower()
	{
		if(invoker==null) return false;
		MOB mob=(MOB)invoker;
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
		MOB mob=(MOB)invoker;
		if(Sense.canBeSeenBy(mob,shadowing)) return false;
		if(!Sense.canBeSeenBy(shadowing,mob)) return false;
		if(mob.location()!=shadowing.location()) return false;
		if(mob.getGroupMembers(new Hashtable()).size()>1) return false;
		return true;
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if(((affect.targetMinor()==Affect.TYP_LEAVE)
		 ||(affect.targetMinor()==Affect.TYP_FLEE))
		&&(stillAShadower())
		&&(stillAShadowee())
		&&(affect.amISource(shadowing))
		&&(affect.amITarget(shadowing.location()))
		&&(affect.othersMessage()!=null))
		{
			String directionWent=affect.othersMessage();
			int x=directionWent.lastIndexOf(" ");
			if(x>=0)
			{
				directionWent=directionWent.substring(x+1);
				int dir=Directions.getDirectionCode(directionWent);
				if((dir>=0)&&(affect.source().location()!=lastRoom))
				{
					MOB mob=(MOB)invoker;
					lastRoom=affect.source().location();
					if(!mob.isMonster())
						mob.session().enque(0,Util.parse(directionWent));
					else
						ExternalPlay.move(mob,dir,false,false);
				}
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(lastTogether==0) return true;
		if((shadowing!=null)&&(invoker!=null)&&(shadowing.location()==invoker.location()))
			lastTogether=System.currentTimeMillis();
		long secondsago=lastTogether-(5*IQCalendar.MILI_SECOND);
		if(lastTogether>secondsago)
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
				invoker.delAffect(this);
				setAffectedOne(shadowing);
				((MOB)invoker).tell("You are no longer shadowing "+shadowing.name()+".");
			}
			shadowing=null;
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Thief_Shadow A=(Thief_Shadow)mob.fetchAffect(ID());
		if(A!=null)
		{
			if(A.shadowing==null)
				mob.delAffect(A);
			else
			{
				Ability AA=A.shadowing.fetchAffect(ID());
				if((AA!=null)&&(AA.invoker()==mob))
				{
					AA.unInvoke();
					return true;
				}
				else
					mob.delAffect(A);
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
		if(mob.getGroupMembers(new Hashtable()).size()>1)
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
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		shadowing=null;
		int levelDiff=target.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*10),auto);

		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_VISUAL,auto?"":"Your attempt to shadow <T-NAMESELF> fails; <T-NAME> spots you!",Affect.MSG_OK_VISUAL,auto?"":"You spot <S-NAME> trying to shadow you.",Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,this,auto?Affect.MSG_OK_VISUAL:Affect.MSG_THIEF_ACT,"You are now shadowing <T-NAME>.  Enter 'shadow' again to disengage.",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				shadowing=target;
				if(beneficialAffect(mob,target,Integer.MAX_VALUE-1000))
				{
					A=(Thief_Shadow)target.fetchAffect(ID());
					if(A!=null)
					{
						mob.addAffect(A);
						A.shadowing=target;
						A.setAffectedOne(target);
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