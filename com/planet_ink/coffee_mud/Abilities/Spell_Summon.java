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

public class Spell_Summon extends Spell
	implements InvocationDevotion
{
	public Spell_Summon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon";

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
		return new Spell_Summon();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Summon whom?");
			return false;
		}
		String areaName=CommandProcessor.combine(commands,0).trim().toUpperCase();

		if(mob.location().fetchInhabitant(areaName)!=null)
		{
			mob.tell("Better look around first.");
			return false;
		}

		Room newRoom=null;
		MOB target=null;
		for(int m=0;m<MUD.map.size();m++)
		{
			Room room=(Room)MUD.map.elementAt(m);
			target=room.fetchInhabitant(areaName);
			if(target!=null)
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

		boolean success=profficiencyCheck(-(target.envStats().level()*2));

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> summon(s) <T-NAME> in a mighty cry!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Hashtable h=Grouping.getAllFollowers(mob);
				if(h.get(mob.ID())==null)
				   h.put(mob.ID(),mob);

				Room thisRoom=mob.location();
				MOB follower=target;
				FullMsg enterMsg=new FullMsg(follower,thisRoom,this,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,"<S-NAME> appears in a burst of light.");
				FullMsg leaveMsg=new FullMsg(follower,newRoom,this,Affect.HANDS_RECALL,Affect.HANDS_RECALL,Affect.HANDS_RECALL,"<S-NAME> disappear(s) in a great summoning swirl.");
				if(newRoom.okAffect(leaveMsg)&&thisRoom.okAffect(enterMsg))
				{
					follower.makePeace();
					newRoom.delInhabitant(follower);
					newRoom.send(follower,leaveMsg);

					follower.setLocation(thisRoom);
					thisRoom.addInhabitant(follower);
					thisRoom.send(follower,enterMsg);
					BasicSenses.look(follower,null,true);
				}
			}

		}
		else
			beneficialFizzle(mob,null,"<S-NAME> attempt(s) to summon <T-NAME>, but fail(s).");


		// return whether it worked
		return success;
	}
}