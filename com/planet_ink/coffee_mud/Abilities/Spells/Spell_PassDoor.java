package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_PassDoor extends Spell
{
	public Spell_PassDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Pass Door";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_PassDoor();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		String whatToOpen=Util.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode<0)
		{
			mob.tell("Pass which direction?!");
			return false;
		}

		Exit exit=mob.location().getExitInDir(dirCode);
		Room room=mob.location().getRoomInDir(dirCode);

		if((exit==null)||(room==null)||((exit!=null)&&(!Sense.canBeSeenBy(exit,mob))))
		{
			mob.tell("You can't see anywhere to pass that way.");
			return false;
		}
		Exit opExit=room.getPairedExit(dirCode);

		if(exit.isOpen())
		{
			mob.tell("But it looks free and clear that way!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> walk(s) "+Directions.getDirectionName(dirCode)+", but go(es) no further.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType,"<S-NAME> shimmer(s) and pass(es) "+Directions.getDirectionName(dirCode)+".");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				boolean open=exit.isOpen();
				boolean locked=exit.isLocked();
				exit.setDoorsNLocks(exit.hasADoor(),true,exit.defaultsClosed(),exit.hasALock(),false,exit.defaultsLocked());
				if(opExit!=null)
					opExit.setDoorsNLocks(exit.hasADoor(),true,exit.defaultsClosed(),exit.hasALock(),false,exit.defaultsLocked());
				mob.tell("\n\r\n\r");
				ExternalPlay.move(mob,dirCode,false);
				exit.setDoorsNLocks(exit.hasADoor(),open,exit.defaultsClosed(),exit.hasALock(),locked,exit.defaultsLocked());
				if(opExit!=null)
					opExit.setDoorsNLocks(exit.hasADoor(),open,exit.defaultsClosed(),exit.hasALock(),locked,exit.defaultsLocked());
			}
		}

		return success;
	}
}