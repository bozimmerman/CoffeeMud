package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_Teleport extends Spell
	implements InvocationDevotion
{
	public Spell_Teleport()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Teleport";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(10);

		addQualifyingClass(new Mage().ID(),10);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Teleport();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Teleport to what area?");
			return false;
		}
		String areaName=CommandProcessor.combine(commands,0).trim().toUpperCase();
		int numRooms=0;
		for(int m=0;m<MUD.map.size();m++)
		{
			Room room=(Room)MUD.map.elementAt(m);
			if(room.getAreaID().toUpperCase().startsWith(areaName))
				numRooms++;
		}

		if(numRooms==0)
		{
			mob.tell("You don't know of an area called '"+CommandProcessor.combine(commands,0)+"'.");
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			int roomNum=(int)Math.round(Math.random()*numRooms);
			for(int m=0;m<MUD.map.size();m++)
			{
				Room room=(Room)MUD.map.elementAt(m);
				if(room.getAreaID().toUpperCase().startsWith(areaName))
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
			FullMsg enterMsg=new FullMsg(mob,newRoom,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null);
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

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a teleportation spell.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Hashtable h=Grouping.getAllFollowers(mob);
				if(h.get(mob.ID())==null)
				   h.put(mob.ID(),mob);

				Room thisRoom=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					if((follower.isMonster())||(follower==mob))
					{
						FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,"<S-NAME> appears in a puff of smoke.");
						FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.HANDS_RECALL,Affect.HANDS_RECALL,Affect.HANDS_RECALL,"<S-NAME> disappear(s) in a puff of smoke.");
						if(thisRoom.okAffect(leaveMsg)&&newRoom.okAffect(enterMsg))
						{
							if(follower.isInCombat())
							{
								Movement.flee(follower,"NOWHERE");
								follower.makePeace();
							}
							thisRoom.delInhabitant(follower);
							thisRoom.send(follower,leaveMsg);

							follower.setLocation(newRoom);
							newRoom.addInhabitant(follower);
							newRoom.send(follower,enterMsg);
							follower.tell("\n\r\n\r");
							BasicSenses.look(follower,null,true);
						}
					}
				}
			}

		}
		else
			beneficialFizzle(mob,null,"<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
