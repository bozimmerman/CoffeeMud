package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Thief_Observation extends ThiefSkill
{

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

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Observation();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_SNEAKERS);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already observing.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,null,this,auto?Affect.MSG_OK_ACTION:(Affect.MSG_DELICATE_HANDS_ACT|Affect.ACT_EYES),"<S-NAME> open(s) <S-HIS-HER> eyes and observe(s) <S-HIS-HER> surroundings carefully.");
		if(!success)
			return beneficialVisualFizzle(mob,null,"<S-NAME> look(s) around carefully, but become(s) distracted.");
		else
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,0);
		}
		return success;
	}
}
