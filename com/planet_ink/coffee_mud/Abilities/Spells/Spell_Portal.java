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

public class Spell_Portal extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Portal";
	}

	private final static String localizedName = CMLib.lang().L("Portal");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL-90;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	Room newRoom=null;
	Room oldRoom=null;

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(newRoom!=null)
			{
				newRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The swirling portal closes."));
				newRoom.rawDoors()[Directions.GATE]=null;
				newRoom.setRawExit(Directions.GATE,null);
			}
			if(oldRoom!=null)
			{
				oldRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("The swirling portal closes."));
				oldRoom.rawDoors()[Directions.GATE]=null;
				oldRoom.setRawExit(Directions.GATE,null);
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		newRoom=null;
		oldRoom=null;

		if((auto||mob.isMonster())&&((commands.size()<1)||((commands.get(0)).equals(mob.name()))))
		{
			commands.clear();
			if(text().trim().length()>0)
				commands.add(text());
			else
				commands.add(CMLib.map().getRandomRoom().displayText());
		}
		if(commands.size()<1)
		{
			mob.tell(L("Create a portal to where?"));
			return false;
		}
		if((mob.location().getRoomInDir(Directions.GATE)!=null)
		||(mob.location().getExitInDir(Directions.GATE)!=null))
		{
			mob.tell(L("A portal cannot be created here."));
			return false;
		}
		final String areaName=CMParms.combine(commands,0).trim().toUpperCase();
		oldRoom=null;
		newRoom=null;
		int tries=0;
		while(((++tries)<10000))
		{
			final Room room=CMLib.map().getRandomRoom();
			if((CMLib.flags().canAccess(mob,room))
			&&(CMLib.english().containsString(room.displayText(mob),areaName)))
			{
			   newRoom=room;
			   break;
			}
		}
		if(newRoom==null)
		{
			try
			{
				final List<Room> rooms=CMLib.map().findRooms(CMLib.map().rooms(), mob, areaName, true, 10);
				if(rooms.size()>0)
					newRoom=rooms.get(CMLib.dice().roll(1,rooms.size(),-1));
			}
			catch(final NoSuchElementException nse)
			{
			}
		}

		if(newRoom==null)
		{
			mob.tell(L("You don't know of a place called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		int profNeg=0;
		for(int i=0;i<newRoom.numInhabitants();i++)
		{
			final MOB t=newRoom.fetchInhabitant(i);
			if(t!=null)
			{
				int adjustment=t.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
				if(t.isMonster())
					adjustment=adjustment*3;
				profNeg+=adjustment;
			}
		}
		profNeg+=newRoom.numItems()*20;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,-profNeg,auto);

		if((success)
		&&((newRoom.getRoomInDir(Directions.GATE)==null)
		&&(newRoom.getExitInDir(Directions.GATE)==null)))
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),L("^S<S-NAME> evoke(s) a blinding, swirling portal here.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,newRoom,this,verbalCastCode(mob,newRoom,auto),L("A blinding, swirling portal appears here."));
			if((mob.location().okMessage(mob,msg))&&(newRoom.okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				newRoom=(Room)msg2.target();
				newRoom.send(mob,msg2);
				final Exit e=CMClass.getExit("GenExit");
				e.setDescription(L("A swirling portal to somewhere"));
				e.setDisplayText(L("A swirling portal to somewhere"));
				e.setDoorsNLocks(false,true,false,false,false,false);
				e.setExitParams("portal","close","open","closed.");
				e.setName(L("a swirling portal"));
				final Ability A1=CMClass.getAbility("Prop_RoomView");
				if(A1!=null)
				{
					A1.setMiscText(CMLib.map().getExtendedRoomID(newRoom));
					e.addNonUninvokableEffect(A1);
				}
				final Exit e2=(Exit)e.copyOf();
				final Ability A2=CMClass.getAbility("Prop_RoomView");
				if(A2!=null)
				{
					A2.setMiscText(CMLib.map().getExtendedRoomID(mob.location()));
					e2.addNonUninvokableEffect(A2);
				}
				mob.location().rawDoors()[Directions.GATE]=newRoom;
				newRoom.rawDoors()[Directions.GATE]=mob.location();
				mob.location().setRawExit(Directions.GATE,e);
				newRoom.setRawExit(Directions.GATE,e2);
				oldRoom=mob.location();
				beneficialAffect(mob,e,asLevel,15);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to evoke a portal, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
