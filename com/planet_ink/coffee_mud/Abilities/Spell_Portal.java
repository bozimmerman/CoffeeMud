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

public class Spell_Portal extends Spell
	implements InvocationDevotion
{
	public Spell_Portal()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Portal";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(18);

		addQualifyingClass(new Mage().ID(),18);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Portal();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Create a portal to where?");
			return false;
		}
		String areaName=CommandProcessor.combine(commands,0).trim().toUpperCase();
		Room newRoom=null;
		for(int m=0;m<MUD.map.size();m++)
		{
			Room room=(Room)MUD.map.elementAt(m);
			if(Util.containsString(room.displayText().toUpperCase(),areaName))
			{
			   newRoom=room;
			   break;
			}
		}

		if(newRoom==null)
		{
			mob.tell("You don't know of an place called '"+CommandProcessor.combine(commands,0)+"'.");
			return false;
		}

		int profNeg=0;
		profNeg+=newRoom.numInhabitants()*20;
		profNeg+=newRoom.numItems()*20;

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(-profNeg);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> evoke(s) a blinding portal.");
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
		else
			beneficialFizzle(mob,null,"<S-NAME> attempt(s) to evoke a portal, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}