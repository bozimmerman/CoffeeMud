package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_Listen extends ThiefSkill
{

	public Thief_Listen()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Listen";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("LISTEN");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(11);

		addQualifyingClass(new Thief().ID(),11);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Listen();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		int dirCode=Directions.getDirectionCode(CommandProcessor.combine(commands,0));
		
		if(!Sense.canHear(mob))
		{
			mob.tell("You don't hear anything.");
			return false;
		}
		
		Room room=null;
		if(dirCode<0)
			room=mob.location();
		else
		{
			if((mob.location().getRoom(dirCode)==null)||(mob.location().getExit(dirCode)==null))
			{
				mob.tell("Listen which direction?");
				return false;
			}
			room=mob.location().getRoom(dirCode);
			if((room.domainType()&128)==Room.OUTDOORS)
			{
				mob.tell("You can't listen to that.");
				return false;
			}
		}
		
		if(!super.invoke(mob,commands))
			return false;

		boolean success=false;
		FullMsg msg=new FullMsg(mob,null,null,Affect.HANDS_DELICATE,"You listen "+Directions.getDirectionName(dirCode)+".",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> listens "+Directions.getDirectionName(dirCode)+".");
			success=profficiencyCheck(0);
			int numberHeard=0;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((!Sense.isSneaking(inhab))&&(!Sense.isHidden(inhab))&&(inhab!=mob))
					numberHeard++;
			}
			if((success)&&(numberHeard>0))
			{
				if(profficiency()>75)
					mob.tell("You definitely the movement of "+numberHeard+" creature(s).");
				else
					mob.tell("You definitely hear something.");
			}
			else
				mob.tell("You don't hear anything.");
		}
		return success;
	}

}