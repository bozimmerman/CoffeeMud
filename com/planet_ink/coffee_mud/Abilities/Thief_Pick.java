package com.planet_ink.coffee_mud.Abilities;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_Pick extends ThiefSkill
{

	public Thief_Pick()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Pick lock";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("PICK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		addQualifyingClass(new Thief().ID(),5);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Pick();
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

		if(!success)
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to pick "+unlockThis.name()+" and fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.HANDS_UNLOCK,Affect.HANDS_UNLOCK,Affect.VISUAL_WNOISE,null);
			if(mob.location().okAffect(msg))
			{
				msg=new FullMsg(mob,unlockThis,null,Affect.HANDS_UNLOCK,Affect.HANDS_UNLOCK,Affect.VISUAL_WNOISE,"<S-NAME> pick(s) the lock on "+unlockThis.name()+".");
				mob.location().send(mob,msg);
			}
		}

		return success;
	}
}