package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Fighter().ID(),13);
		addQualifyingClass(new Ranger().ID(),13);
		addQualifyingClass(new Paladin().ID(),13);
		addQualifyingClass(new Thief().ID(),13);
		addQualifyingClass(new Bard().ID(),9);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Trip();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SITTING);
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
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("You regain your feet.");
		Movement.standIfNecessary(mob);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		if((Sense.isSitting(target)||Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" is already on the floor!");
			return false;
		}

		if(Sense.isFlying(target))
		{
			mob.tell(target.name()+" is flying and can't be tripped!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(-(target.charStats().getDexterity()));

		String str=null;
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_WORDS,Affect.STRIKE_JUSTICE,Affect.VISUAL_WNOISE,"<S-NAME> trip(s) <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				maliciousAffect(mob,target,2,-1);
				target.tell("You hit the floor!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to trip <T-NAME>, but end(s) up looking silly.");
		return success;
	}
}
