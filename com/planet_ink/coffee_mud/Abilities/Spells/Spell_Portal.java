package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Portal extends Spell
{
	Room newRoom=null;
	Room oldRoom=null;
	public Spell_Portal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Portal";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=0;
		canTargetCode=0;

		baseEnvStats().setLevel(18);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Portal();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_EVOCATION;
	}

	public void unInvoke()
	{
		if(newRoom!=null)
		{
			newRoom.showHappens(Affect.MSG_OK_VISUAL,"The swirling portal closes.");
			newRoom.rawDoors()[Directions.GATE]=null;
			newRoom.rawExits()[Directions.GATE]=null;
		}
		if(oldRoom!=null)
		{
			oldRoom.showHappens(Affect.MSG_OK_VISUAL,"The swirling portal closes.");
			oldRoom.rawDoors()[Directions.GATE]=null;
			oldRoom.rawExits()[Directions.GATE]=null;
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("Create a portal to where?");
			return false;
		}
		if((mob.location().getRoomInDir(Directions.GATE)!=null)
		||(mob.location().getExitInDir(Directions.GATE)!=null))
		{
			mob.tell("A portal cannot be created here.");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room room=CMMap.getRoom(m);
			if(CoffeeUtensils.containsString(room.displayText().toUpperCase(),areaName))
			{
			   newRoom=room;
			   break;
			}
		}

		if(newRoom==null)
		{
			mob.tell("You don't know of an place called '"+Util.combine(commands,0)+"'.");
			return false;
		}

		int profNeg=0;
		profNeg+=newRoom.numInhabitants()*20;
		profNeg+=newRoom.numItems()*20;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(-profNeg,auto);

		if((success)
		&&((newRoom.getRoomInDir(Directions.GATE)==null)
		&&(newRoom.getExitInDir(Directions.GATE)==null)))
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,"<S-NAME> evoke(s) a blinding, swirling portal here.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Exit e=CMClass.getExit("GenExit");
				e.setDescription("A swirling portal to somewhere");
				e.setDisplayText("A swirling portal to somewhere");
				e.setDoorsNLocks(false,true,false,false,false,false);
				e.setExitParams("portal","close","open","closed.");
				e.setName("portal");
				mob.location().rawDoors()[Directions.GATE]=newRoom;
				newRoom.rawDoors()[Directions.GATE]=mob.location();
				mob.location().rawExits()[Directions.GATE]=e;
				newRoom.rawExits()[Directions.GATE]=e;
				oldRoom=mob.location();
				beneficialAffect(mob,e,5);
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to evoke a portal, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}