package com.planet_ink.coffee_mud.Abilities.Ranger;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Ranger_Hide extends StdAbility
{
	public String ID() { return "Ranger_Hide"; }
	public String name(){ return "Woodland Hide";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"WHIDE"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Ranger_Hide();}
	public int classificationCode(){return Ability.SKILL;}

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
			
			if(((Util.bset(affect.sourceMajor(),Affect.MASK_SOUND)
				 ||(affect.sourceMinor()==Affect.TYP_SPEAK)
				 ||(affect.sourceMinor()==Affect.TYP_ENTER)
				 ||(affect.sourceMinor()==Affect.TYP_LEAVE)
				 ||(affect.sourceMinor()==Affect.TYP_RECALL)))
			 &&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
			 &&(affect.sourceMinor()!=Affect.TYP_EXAMINESOMETHING)
			 &&(affect.sourceMajor()>0))
			{
				unInvoke();
				mob.recoverEnvStats();
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
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

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
		
		if((((mob.location().domainType()&Room.INDOORS)>0))&&(!auto))
		{
			mob.tell("You only know how to hide outdoors.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)&&(!auto))
		{
			mob.tell("You don't know how to hide in a place like this.");
			return false;
		}

		Hashtable H=mob.getGroupMembers(new Hashtable());
		int highestLevel=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)&&((M!=mob)&&(!H.contains(M)))&&(highestLevel<M.envStats().level()))
				highestLevel=mob.envStats().level();
		}
		int levelDiff=mob.envStats().level()-highestLevel;

		String str="You creep into some foliage and remain completely still.";
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_ROCKS)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_DESERT))
			str="You creep behind some rocks and remain completely still.";
		   

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