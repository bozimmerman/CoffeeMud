package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Teleport extends Spell
{
	public Spell_Teleport()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Teleport";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(10);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Teleport();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Teleport to what area?");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		int numRooms=0;
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room room=CMMap.getRoom(m);
			if(room.getArea().name().toUpperCase().startsWith(areaName))
				numRooms++;
		}

		if(numRooms==0)
		{
			mob.tell("You don't know of an area called '"+Util.combine(commands,0)+"'.");
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			int roomNum=Dice.roll(1,numRooms,-1);
			for(int m=0;m<CMMap.numRooms();m++)
			{
				Room room=CMMap.getRoom(m);
				if(room.getArea().name().toUpperCase().startsWith(areaName))
				{
					if(roomNum==0)
					{
						newRoom=room;
						break;
					}
					else
						roomNum--;
				}
			}
			FullMsg enterMsg=new FullMsg(mob,newRoom,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null);
			Session session=mob.session();
			mob.setSession(null);
			if(!newRoom.okAffect(enterMsg))
				newRoom=null;
			mob.setSession(session);
			tries++;
		}

		if(newRoom==null)
		{
			mob.tell("Your magic seems unable to take you to that area.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,"<S-NAME> invoke(s) a teleportation spell.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Hashtable h=ExternalPlay.properTargets(this,mob);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					if((follower.isMonster())||(follower==mob))
					{
						FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appears in a puff of smoke.");
						FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
						if(thisRoom.okAffect(leaveMsg)&&newRoom.okAffect(enterMsg))
						{
							if(follower.isInCombat())
							{
								ExternalPlay.flee(follower,"NOWHERE");
								follower.makePeace();
							}
							thisRoom.send(follower,leaveMsg);
							newRoom.bringMobHere(follower,false);
							newRoom.send(follower,enterMsg);
							follower.tell("\n\r\n\r");
							ExternalPlay.look(follower,null,true);
						}
					}
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
