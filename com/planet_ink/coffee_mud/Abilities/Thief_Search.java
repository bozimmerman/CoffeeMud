package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_Search extends ThiefSkill
{
	public Thief_Search()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hide";
		displayText="(Ability to search for all things hidden)";
		miscText="";

		triggerStrings.addElement("SEARCH");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		addQualifyingClass(new Thief().ID(),14);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Search();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.CAN_SEE_HIDDEN);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already aware of hidden things.");
			return false;
		}

		boolean success=profficiencyCheck(0);

		FullMsg msg=new FullMsg(mob,null,this,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> open(s) <S-HIS-HER> eyes and examine(s) <S-HIS-HER> surroundings carefully.");
		if(!success)
			return beneficialFizzle(mob,null,"<S-NAME> look(s) around carefully, but become(s) distracted.");
		else
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,0);
			BasicSenses.look(mob,null,false);
		}
		return success;
	}
}
