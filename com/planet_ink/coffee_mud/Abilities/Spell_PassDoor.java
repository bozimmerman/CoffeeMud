package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_PassDoor extends Spell
	implements AlterationDevotion
{
	public Spell_PassDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Pass Door";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		addQualifyingClass(new Mage().ID(),7);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_PassDoor();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		String whatToOpen=CommandProcessor.combine(commands,0);
		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatToOpen.toUpperCase());
		int dirCode=-1;
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
				dirCode=Directions.getDirectionCode(whatToOpen);
		}

		if(dirCode<0)
		{
			mob.tell("Pass which direction?!");
			return false;
		}

		Exit exit=mob.location().getExit(dirCode);
		Room room=mob.location().getRoom(dirCode);

		if((exit==null)||(room==null)||((exit!=null)&&(!Sense.canBeSeenBy(exit,mob))))
		{
			mob.tell("You can't see anywhere to pass that way.");
			return false;
		}

		if(exit.isOpen())
		{
			mob.tell("But it looks free and clear that way!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;


		boolean success=profficiencyCheck(0);

		if(!success)
			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> walk(s) "+Directions.getDirectionName(dirCode)+", but go(es) no further.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> shimmer(s) and pass(es) "+Directions.getDirectionName(dirCode)+".");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				boolean open=exit.isOpen();
				boolean locked=exit.isLocked();
				if(locked)
				{
					msg=new FullMsg(mob,exit,null,Affect.NO_EFFECT,Affect.HANDS_UNLOCK,Affect.NO_EFFECT,null);
					exit.affect(msg);
				}
				if(!open)
				{
					msg=new FullMsg(mob,exit,null,Affect.NO_EFFECT,Affect.HANDS_OPEN,Affect.NO_EFFECT,null);
					exit.affect(msg);
				}
				mob.tell("\n\r\n\r");
				Movement.move(mob,dirCode,false);
				if(!open)
				{
					msg=new FullMsg(mob,exit,null,Affect.NO_EFFECT,Affect.HANDS_CLOSE,Affect.NO_EFFECT,null);
					exit.affect(msg);
				}
				if(locked)
				{
					msg=new FullMsg(mob,exit,null,Affect.NO_EFFECT,Affect.HANDS_LOCK,Affect.NO_EFFECT,null);
					exit.affect(msg);
				}
			}
		}

		return success;
	}
}