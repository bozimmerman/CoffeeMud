package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_Climb extends StdAbility
{
	
	public boolean successful=false;

	public Skill_Climb()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sneak";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("CLIMB");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Thief().ID(),1);
		addQualifyingClass(new Bard().ID(),3);
		addQualifyingClass(new Fighter().ID(),15);
		addQualifyingClass(new Ranger().ID(),15);
		addQualifyingClass(new Paladin().ID(),15);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Climb();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		int dirCode=Directions.getDirectionCode(CommandProcessor.combine(commands,0));
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

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		FullMsg msg=new FullMsg(mob,null,null,Affect.HANDS_DELICATE,null,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			success=profficiencyCheck(0);
			
			successful=success;
			if(mob.fetchAffect(ID())==null)
				mob.addAffect(this);

			Movement.move(mob,dirCode,false);
			mob.delAffect(this);
		}
		return success;
	}

}
