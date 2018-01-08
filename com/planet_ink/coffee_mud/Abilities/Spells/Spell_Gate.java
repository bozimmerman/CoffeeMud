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

public class Spell_Gate extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Gate";
	}

	private final static String	localizedName	= CMLib.lang().L("Gate");

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
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT + 50;
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

	public boolean isBadRoom(final Room room, final MOB mob, final Room newRoom)
	{
		return (room==null)
		||(room==newRoom)
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
			if(text().length()>0)
				commands.add(text());
			else
			{
				MOB M=null;
				int tries=0;
				while(((++tries)<100)&&(M==null))
				{
					final Room R=CMLib.map().getRandomRoom();
					if(R.numInhabitants()>0)
						M=R.fetchRandomInhabitant();
					if((M!=null)&&(M.name().equals(mob.name())))
						M=null;
				}
				if(M!=null)
					commands.add(M.Name());
			}
		}
		if(commands.size()<1)
		{
			mob.tell(L("Gate to whom?"));
			return false;
		}
		final String areaName=CMParms.combine(commands,0).trim().toUpperCase();

		if(mob.location().fetchInhabitant(areaName)!=null)
		{
			mob.tell(L("Better look around first."));
			return false;
		}

		if(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}

		List<MOB> candidates=new Vector<MOB>();
		MOB target=null;
		try
		{
			candidates=CMLib.map().findInhabitantsFavorExact(CMLib.map().rooms(), mob, areaName, false, 10);
		}
		catch(final NoSuchElementException nse)
		{
		}
		Room newRoom=null;
		if(candidates.size()>0)
		{
			target=candidates.get(CMLib.dice().roll(1,candidates.size(),-1));
			newRoom=target.location();
		}

		if((newRoom==null) || (target == null))
		{
			mob.tell(L("You can't seem to fixate on '@x1', perhaps they don't exist?",CMParms.combine(commands,0)));
			return false;
		}

		int adjustment=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(target.isMonster())
			adjustment=adjustment*3;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,-adjustment,auto);
		String addOn=".";
		if(!success)
		{
			Room room=null;
			int x=0;
			while(isBadRoom(room,mob,newRoom) && ((++x)<1000))
				room=CMLib.map().getRandomRoom();
			if(isBadRoom(room,mob,newRoom))
			{
				beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell."));
				room=null;
			}
			addOn=", but the spell goes AWRY!!";
			newRoom=room;
		}

		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|verbalCastCode(mob,target,auto),L("^S<S-NAME> invoke(s) a teleportation spell@x1^?",addOn));
		if((mob.location().okMessage(mob,msg))&&(newRoom!=null)&&(newRoom.okMessage(mob,msg)))
		{
			mob.location().send(mob,msg);
			final List<MOB> h=properTargetList(mob,givenTarget,false);
			if(h==null)
				return false;

			final Room thisRoom=mob.location();
			for (final MOB follower : h)
			{
				final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,("<S-NAME> appear(s) in a burst of light.")+CMLib.protocol().msp("appear.wav",10));
				final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> disappear(s) in a burst of light."));
				if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CMLib.commands().postFlee(follower,("NOWHERE"));
						follower.makePeace(false);
					}
					thisRoom.send(follower,leaveMsg);
					((Room)enterMsg.target()).bringMobHere(follower,false);
					((Room)enterMsg.target()).send(follower,enterMsg);
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
