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

public class Spell_Gate extends Spell
	implements InvocationDevotion
{
	public Spell_Gate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Gate";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Mage().ID(),13);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Gate();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Gate to whom?");
			return false;
		}
		String areaName=CommandProcessor.combine(commands,0).trim().toUpperCase();

		if(mob.location().fetchInhabitant(areaName)!=null)
		{
			mob.tell("Better look around first.");
			return false;
		}

		Room newRoom=null;
		for(int m=0;m<MUD.map.size();m++)
		{
			Room room=(Room)MUD.map.elementAt(m);
			if(room.fetchInhabitant(areaName)!=null)
			{
				newRoom=room;
				break;
			}
		}

		if(newRoom==null)
		{
			mob.tell("You can't seem to fixate on '"+CommandProcessor.combine(commands,0)+"', perhaps they don't exist?");
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
						FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,"<S-NAME> appears in a burst of light.");
						FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.HANDS_RECALL,Affect.HANDS_RECALL,Affect.HANDS_RECALL,"<S-NAME> disappear(s) in a burst of light.");
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
