package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Hide extends ThiefSkill
{
	public String ID() { return "Thief_Hide"; }
	public String name(){ return "Hide";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"HIDE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Hide();}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			if(((affect.sourceMinor()==Affect.TYP_ENTER)
				||(affect.sourceMinor()==Affect.TYP_LEAVE)
				||(affect.sourceMinor()==Affect.TYP_FLEE)
				||(affect.sourceMinor()==Affect.TYP_RECALL))
			&&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
			&&(affect.sourceMajor()>0))
			{
				unInvoke();
				mob.recoverEnvStats();
			}
			else
			if((abilityCode()==0)
			&&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
			&&(affect.othersMinor()!=Affect.TYP_EXAMINESOMETHING)
			&&(affect.othersMajor()>0))
			{
				if(Util.bset(affect.othersMajor(),Affect.MASK_SOUND))
				{
					unInvoke();
					mob.recoverEnvStats();
				}
				else
				switch(affect.othersMinor())
				{
				case Affect.TYP_SPEAK:
				case Affect.TYP_CAST_SPELL:
					{
						unInvoke();
						mob.recoverEnvStats();
					}
					break;
				case Affect.TYP_OPEN:
				case Affect.TYP_CLOSE:
				case Affect.TYP_LOCK:
				case Affect.TYP_UNLOCK:
				case Affect.TYP_PUSH:
				case Affect.TYP_PULL:
					if((affect.target()!=null)
					&&((affect.target() instanceof Exit)
						||((affect.target() instanceof Item)
						   &&(!affect.source().isMine((Item)affect.target())))))
					{
						unInvoke();
						mob.recoverEnvStats();
					}
					break;
				}
			}
		}
		return;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
		if(Sense.isSneaking(affected))
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_SNEAKING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already hiding.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Hashtable H=mob.getGroupMembers(new Hashtable());
		int highestLevel=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&((M!=mob)&&(!H.contains(M)))&&(highestLevel<M.envStats().level()))
				highestLevel=mob.envStats().level();
		}
		int levelDiff=mob.envStats().level()-highestLevel;

		String str="You creep into a shadow and remain completely still.";

		boolean success=profficiencyCheck(levelDiff*10,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to hide and fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,null,this,auto?Affect.MSG_OK_ACTION:(Affect.MSG_DELICATE_HANDS_ACT|Affect.MASK_MOVE),str,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Ability newOne=(Ability)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
			}
			else
				success=false;
		}
		return success;
	}
}
