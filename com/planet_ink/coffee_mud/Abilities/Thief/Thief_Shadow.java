package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Shadow extends ThiefSkill
{
	public MOB shadowing=null;
	private Vector moves=null;
	public Thief_Shadow()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Shadow";
		miscText="";
		displayText="check";

		triggerStrings.addElement("SHADOW");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(2);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Shadow();
	}

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
	public void affect(Affect affect)
	{
		super.affect(affect);
		if(((affect.targetMinor()==Affect.TYP_LEAVE)
		 ||(affect.targetMinor()==Affect.TYP_FLEE))
		&&(moves!=null)
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
				synchronized(moves)
				{
					directionWent=directionWent.substring(x+1);
					moves.addElement(new Integer(Directions.getDirectionCode(directionWent)));
				}
			}
		}
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if(tickID!=Host.MOB_TICK)
			return true;
		if((stillAShadower())&&(stillAShadowee()))
		{
			MOB mob=(MOB)invoker;
			while((moves!=null)&&(moves.size()>0)&&(mob.location()!=shadowing.location()))
			{
				synchronized(moves)
				{
					Integer move=(Integer)moves.firstElement();
					moves.removeElement(move);
					if((move.intValue()>=0)&&stillAShadower()&&stillAShadowee())
					{
						mob.tell("You shadow "+shadowing.name()+" "+Directions.getDirectionName(move.intValue())+".");
						ExternalPlay.move(mob,move.intValue(),false);
					}
					else
					{
						moves.clear();
						break;
					}
				}
			}
		}
		if(!canShadow())
		{
			unInvoke();
			return false;
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
	}
	
	public void unInvoke()
	{
		if((invoker!=null)&&(shadowing!=null))
		{
			invoker.delAffect(this);
			setAffectedOne(shadowing);
			((MOB)invoker).tell("You are no longer shadowing "+shadowing.name()+".");
		}
		shadowing=null;
		moves=null;
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
		moves=null;
		int levelDiff=target.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*10),auto);

		if(!success)
		{
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_OK_VISUAL,auto?"":"Your attempt to shadow <T-NAMESELF> fails; <T-NAME> spots you!",Affect.MSG_OK_VISUAL,auto?"":"You spot <S-NAME> trying to shadow you.",Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,null,auto?Affect.MSG_OK_VISUAL:Affect.MSG_DELICATE_HANDS_ACT,"You are now shadowing <T-NAME>.  Enter 'shadow' again to disengage.",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				moves=new Vector();
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