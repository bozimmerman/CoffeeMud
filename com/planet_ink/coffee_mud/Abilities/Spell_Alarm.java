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

public class Spell_Alarm extends Spell
	implements EvocationDevotion
{
	Room myRoomContainer=null;

	boolean waitingForLook=false;

	public Spell_Alarm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Alarm";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		addQualifyingClass(new Mage().ID(),14);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}


	public Environmental newInstance()
	{
		return new Spell_Alarm();
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if(affected==null)
		{
			this.unInvoke();
			return;
		}

		if(affect.source()!=null)
			myRoomContainer=affect.source().location();

		if(affect.source()==invoker)
			return;

		if(affect.amITarget(affected))
		{
			myRoomContainer.show(invoker,null,Affect.SOUND_WORDS,"A HORRENDOUS ALARM GOES OFF, WHICH SEEMS TO BE COMING FROM "+affected.name().toUpperCase()+"!!!");
			invoker.tell("The alarm on your "+affected.name()+" has gone off.");
			unInvoke();
		}

	}


	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Set an alarm on what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(target instanceof MOB)
		{
			mob.tell("You can't set an alarm on that.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> touch(es) <T-NAME> very lightly.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				myRoomContainer=mob.location();
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> touch(es) <T-NAME> very lightly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}