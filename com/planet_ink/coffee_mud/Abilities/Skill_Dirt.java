package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_Dirt extends StdAbility
{
	boolean doneTicking=false;

	public Skill_Dirt()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dirt";
		displayText="(Dirt in your eyes)";
		miscText="";

		triggerStrings.addElement("DIRT");

		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(11);

		addQualifyingClass(new Fighter().ID(),11);
		addQualifyingClass(new Ranger().ID(),11);
		addQualifyingClass(new Paladin().ID(),11);
		addQualifyingClass(new Thief().ID(),22);
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

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(-(target.charStats().getDexterity()));

		String str=null;
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_WORDS,Affect.STRIKE_JUSTICE,Affect.VISUAL_WNOISE,"<S-NAME> kick(s) dirt at <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				maliciousAffect(mob,target,3,-1);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> is blinded!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to kick dirt at <T-NAME>, but miss(es).");
		return success;
	}
}
