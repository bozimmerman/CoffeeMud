package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Trip extends StdAbility
{
	boolean doneTicking=false;

	public Skill_Trip()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Trip";
		displayText="(Tripped)";
		miscText="";

		triggerStrings.addElement("TRIP");

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Trip();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((doneTicking)&&(affect.amISource(mob)))
			unInvoke();
		else
		if(affect.amISource(mob)&&(affect.sourceMinor()==Affect.TYP_STAND))
			return false;
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		doneTicking=true;
		super.unInvoke();
		if(!mob.amDead())
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> regain(s) <S-HIS-HER> feet.");
			else
				mob.tell("You regain your feet.");
			ExternalPlay.standIfNecessary(mob);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((Sense.isSitting(target)||Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" is already on the floor!");
			return false;
		}
		
		if((!Sense.aliveAwakeMobile(mob,true)||(Sense.isSitting(mob))))
		{
			mob.tell("You need to stand up!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to trip!");
			return false;
		}
		if(Sense.isFlying(target))
		{
			mob.tell(target.name()+" is flying and can't be tripped!");
			return false;
		}

		if(target.riding()!=null)
		{
			mob.tell(target.name()+" is riding "+mob.riding()+" and can't be tripped!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>0) 
			levelDiff=levelDiff*5;
		else 
			levelDiff=0;
		int adjustment=(-levelDiff)+(-(35+((int)Math.round((new Integer(target.charStats().getStat(CharStats.DEXTERITY)).doubleValue()-9.0)*3.0))));
		boolean success=profficiencyCheck(adjustment,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.ACT_GENERAL:0),auto?"<T-NAME> trip(s)!":"<S-NAME> trip(s) <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				maliciousAffect(mob,target,2,-1);
				target.tell("You hit the floor!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to trip <T-NAMESELF>, but fail(s).");
		return success;
	}
}
