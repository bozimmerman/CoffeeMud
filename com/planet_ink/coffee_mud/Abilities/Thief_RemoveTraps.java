package com.planet_ink.coffee_mud.Abilities;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_RemoveTraps extends ThiefSkill
{

	public Thief_RemoveTraps()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Remove Traps";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("DETRAP");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(9);

		addQualifyingClass(new Thief().ID(),9);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_DetectTraps();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		String whatTounlock=CommandProcessor.combine(commands,0);
		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatTounlock.toUpperCase());
		Environmental unlockThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
			{
				int dirCode=Directions.getDirectionCode(whatTounlock);
				unlockThis=mob.location().getExit(dirCode);
			}
		}
		if(unlockThis==null)
			unlockThis=mob.location().fetchFromMOBRoom(mob,null,whatTounlock);

		if(unlockThis==null)
		{
			mob.tell("You don't see that here.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()-unlockThis.envStats().level()-envStats().level())*3));
		Trap theTrap=null;
		for(int a=0;a<unlockThis.numAffects();a++)
		{
			Ability A=unlockThis.fetchAffect(a);
			if(A instanceof Trap)
				theTrap=(Trap)A;
		}

		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to safely deactivate a trap on "+unlockThis.name()+".");

		if(success)
			theTrap.sprung=true;

		mob.tell("You have completed your attempt.");

		return success;
	}
}