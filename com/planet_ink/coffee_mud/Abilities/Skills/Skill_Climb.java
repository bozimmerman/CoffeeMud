package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Climb extends StdAbility
{

	public Skill_Climb()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Climb";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("CLIMB");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass("Thief",1);
		addQualifyingClass("Bard",3);
		addQualifyingClass("Fighter",15);
		addQualifyingClass("Ranger",15);
		addQualifyingClass("Paladin",15);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Climb();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_CLIMBING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int dirCode=Directions.getDirectionCode(Util.combine(commands,0));
		if(dirCode<0)
		{
			mob.tell("Climb where?");
			return false;
		}
		if((dirCode!=Directions.UP)&&(dirCode!=Directions.DOWN))
		{
			mob.tell("You can only climb up or down!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_DELICATE_HANDS_ACT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0,auto);

			if(mob.fetchAffect(ID())==null)
			{
				mob.addAffect(this);
				mob.recoverEnvStats();
			}

			ExternalPlay.move(mob,dirCode,false);
			mob.delAffect(this);
			mob.recoverEnvStats();
			if(!success)
				mob.location().affect(new FullMsg(mob,mob.location(),Affect.ACT_MOVE|Affect.TYP_GENERAL,null));
		}
		return success;
	}

}
