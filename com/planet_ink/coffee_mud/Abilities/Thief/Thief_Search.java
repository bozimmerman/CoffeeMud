package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Search extends ThiefSkill
{
	public Thief_Search()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Search";
		displayText="(Ability to search for all things hidden)";
		miscText="";

		triggerStrings.addElement("SEARCH");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Search();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_HIDDEN);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already aware of hidden things.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,"<S-NAME> open(s) <S-HIS-HER> eyes and examine(s) <S-HIS-HER> surroundings carefully.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> look(s) around carefully, but become(s) distracted.");
		else
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,0);
			mob.envStats().setSensesMask(mob.envStats().sensesMask()|Sense.CAN_SEE_HIDDEN);
			mob.envStats().setSensesMask(mob.envStats().sensesMask()|Sense.CAN_SEE_SNEAKERS);
			ExternalPlay.look(mob,null,false);
			mob.recoverEnvStats();
		}
		return success;
	}
}
