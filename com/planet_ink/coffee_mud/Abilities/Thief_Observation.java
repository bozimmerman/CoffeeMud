package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Thief_Observation extends ThiefSkill
{

	public boolean successfulObservation=false;

	public Thief_Observation()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Observe";
		displayText="(Keen sense of observation)";
		miscText="";

		triggerStrings.addElement("OBSERVE");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(10);

		addQualifyingClass(new Thief().ID(),10);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Observation();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(successfulObservation)
			affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_SNEAKERS);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already observing.");
			return false;
		}

		boolean success=profficiencyCheck(0);
		successfulObservation=success;


		FullMsg msg=new FullMsg(mob,null,this,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> open(s) <S-HIS-HER> eyes and observe(s) <S-HIS-HER> surroundings carefully.");
		if(!success)
			return beneficialFizzle(mob,null,"<S-NAME> look(s) around carefully, but become(s) distracted.");
		else
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,0);
		}
		return success;
	}
}
