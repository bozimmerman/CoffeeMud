package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PlantPass extends Chant
{
	public Chant_PlantPass()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Plant Pass";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_PlantPass();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("You must specify the name of the location of one of your plants.  Use your 'My Plants' skill if necessary.");
			return false;
		}
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		
		Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if(myPlant==null)
		{
			mob.tell("There doesn't appear to be any of your plants here to travel through.");
			return false;
		}

		Vector candidates=Druid_MyPlants.myPlantRooms(mob);
		Room newRoom=null;
		for(int m=0;m<candidates.size();m++)
		{
			Room room=(Room)candidates.elementAt(m);
			if(CoffeeUtensils.containsString(room.displayText().toUpperCase(),areaName))
			{
			   newRoom=room;
			   break;
			}
		}
		if(newRoom==null)
		{
			mob.tell("You can't seem to fixate on a place called '"+Util.combine(commands,0)+"', perhaps you have nothing growing there?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,myPlant,this,affectType,"<S-NAME> chant(s) to <T-NAMESELF> and is drawn into it!");
			if((mob.location().okAffect(msg))&&(newRoom.okAffect(msg)))
			{
				mob.location().send(mob,msg);
				Hashtable h=ExternalPlay.properTargets(this,mob,false);
				if(h==null) return false;

				Room thisRoom=mob.location();
				for(Enumeration f=h.elements();f.hasMoreElements();)
				{
					MOB follower=(MOB)f.nextElement();
					if((follower.isMonster())||(follower==mob))
					{
						FullMsg enterMsg=new FullMsg(follower,newRoom,this,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> appear(s) in a burst of light.");
						FullMsg leaveMsg=new FullMsg(follower,thisRoom,this,Affect.MSG_LEAVE|Affect.MASK_MAGIC,"<S-NAME> disappear(s) in a burst of light.");
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
			beneficialVisualFizzle(mob,myPlant,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
