package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class Spell_Teleport extends Spell
{
	public String ID() { return "Spell_Teleport"; }
	public String name(){return "Teleport";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			commands.addElement(CMLib.map().getRandomArea().Name());
		}
		if(commands.size()<1)
		{
			mob.tell("Teleport to what area?");
			return false;
		}
		String areaName=CMParms.combine(commands,0).trim().toUpperCase();
		Area A=CMLib.map().findArea(areaName);
		Vector candidates=new Vector();
		if(A!=null) candidates.addAll(CMParms.makeVector(A.getProperMap()));
		for(int c=candidates.size()-1;c>=0;c--)
			if(!CMLib.flags().canAccess(mob,(Room)candidates.elementAt(c)))
				candidates.removeElementAt(c);

		if(candidates.size()==0)
		{
			mob.tell("You don't know of an area called '"+CMParms.combine(commands,0)+"'.");
			return false;
		}

		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			mob.tell("You need to stand up!");
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			newRoom=(Room)candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
			if(((newRoom.roomID().length()==0)&&(CMLib.dice().rollPercentage()>50))
			||((newRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)&&(CMLib.dice().rollPercentage()>10)))
			{
				newRoom=null;
				continue;
			}
			CMMsg enterMsg=CMClass.getMsg(mob,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
			Session session=mob.session();
			mob.setSession(null);
			if(!newRoom.okMessage(mob,enterMsg))
				newRoom=null;
			mob.setSession(session);
			tries++;
		}

		if(newRoom==null)
		{
			mob.tell("Your magic seems unable to take you to that area.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			Room room=null;
			int x=0;
			while((room==null)
            ||(room==newRoom)
            ||(room.getArea()==newRoom.getArea())
            ||((++x)>1000)
            ||(room==mob.location())
            ||(!CMLib.flags().canAccess(mob,room))
            ||(CMLib.law().getLandTitle(room)!=null))
				room=CMLib.map().getRandomRoom();
			if(room==null)
				beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell.");
			newRoom=room;
		}

		CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MOVE|verbalCastCode(mob,newRoom,auto),"^S<S-NAME> invoke(s) a teleportation spell.^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			HashSet h=properTargets(mob,givenTarget,false);
			if(h==null) return false;

			Room thisRoom=mob.location();
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB follower=(MOB)f.next();
				CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears in a puff of smoke."+CMProps.msp("appear.wav",10));
				CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a puff of smoke.");
				if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CMLib.commands().postFlee(follower,("NOWHERE"));
						follower.makePeace();
					}
					thisRoom.send(follower,leaveMsg);
					newRoom.bringMobHere(follower,false);
					newRoom.send(follower,enterMsg);
					follower.tell("\n\r\n\r");
					CMLib.commands().postLook(follower,true);
				}
			}
		}



		// return whether it worked
		return success;
	}
}
