package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_Teleport extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Teleport";
	}

	private final static String localizedName = CMLib.lang().L("Teleport");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private boolean isBadRoom(final Room room, final MOB mob, final Room newRoom)
	{
		return (room==null)
		||(room==newRoom)
		||(room.getArea()==newRoom.getArea())
		||(room==mob.location())
		||(!CMLib.flags().canAccess(mob,room))
		||(CMLib.law().getLandTitle(room)!=null);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||((commands.get(0)).equals(mob.name()))))
		{
			commands.clear();
			if((text().length()>0)&&(CMLib.map().findArea(text())!=null))
				commands.add(text());
			else
				commands.add(CMLib.map().getRandomArea().Name());
		}
		if(commands.size()<1)
		{
			mob.tell(L("Teleport to what area?"));
			return false;
		}
		final String areaName=CMParms.combine(commands,0).trim().toUpperCase();
		final Area A=CMLib.map().findArea(areaName);
		final Vector<Room> candidates=new Vector<Room>();
		if(A!=null)
			candidates.addAll(new XVector<Room>(A.getProperMap()));
		for(int c=candidates.size()-1;c>=0;c--)
		{
			if(!CMLib.flags().canAccess(mob,candidates.elementAt(c)))
				candidates.removeElementAt(c);
		}

		if(candidates.size()==0)
		{
			mob.tell(L("You don't know of an area called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			newRoom=candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
			if(((newRoom.roomID().length()==0)&&(CMLib.dice().rollPercentage()>50))
			||((newRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)&&(CMLib.dice().rollPercentage()>10)))
			{
				newRoom=null;
				continue;
			}
			final CMMsg enterMsg=CMClass.getMsg(mob,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
			final Session session=mob.session();
			mob.setSession(null);
			if(!newRoom.okMessage(mob,enterMsg))
				newRoom=null;
			mob.setSession(session);
			tries++;
		}

		if(newRoom==null)
		{
			mob.tell(L("Your magic seems unable to take you to that area."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			Room room=null;
			int x=0;
			while(isBadRoom(room,mob,newRoom) && ((++x)<1000))
				room=CMLib.map().getRandomRoom();
			if(isBadRoom(room,mob,newRoom))
				beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell."));
			newRoom=room;
		}

		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MOVE|verbalCastCode(mob,newRoom,auto),L("^S<S-NAME> invoke(s) a teleportation spell.^?"));
		if(mob.location().okMessage(mob,msg)&&(newRoom!=null))
		{
			mob.location().send(mob,msg);
			final List<MOB> h=properTargetList(mob,givenTarget,false);
			if(h==null)
				return false;

			final Room thisRoom=mob.location();
			for (final MOB follower : h)
			{
				final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> appears in a puff of smoke.@x1",CMLib.protocol().msp("appear.wav",10)));
				final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> disappear(s) in a puff of smoke."));
				if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CMLib.commands().postFlee(follower,("NOWHERE"));
						follower.makePeace(false);
					}
					thisRoom.send(follower,leaveMsg);
					newRoom.bringMobHere(follower,false);
					newRoom.send(follower,enterMsg);
					follower.tell(L("\n\r\n\r"));
					CMLib.commands().postLook(follower,true);
				}
				else
				if(follower==mob)
					break;
			}
		}

		// return whether it worked
		return success;
	}
}
