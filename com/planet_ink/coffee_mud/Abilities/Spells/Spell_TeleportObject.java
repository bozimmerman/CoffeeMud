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
   Copyright 2008-2018 Bo Zimmerman

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

public class Spell_TeleportObject extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_TeleportObject";
	}

	private final static String localizedName = CMLib.lang().L("Teleport Object");

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

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||((commands.get(0)).equals(mob.name()))))
		{
			commands.clear();
			if(mob.numItems()>0)
				commands.add(mob.getRandomItem().Name());
			commands.add(CMLib.map().getRandomArea().Name());
		}
		final Room oldRoom=mob.location();
		if(commands.size()<2)
		{
			mob.tell(L("Teleport what object to what place or person?"));
			return false;
		}
		final String objectName=commands.get(0);
		final Item target=mob.findItem(null,objectName);
		if(target==null)
		{
			mob.tell(L("You don't seem to have an item '@x1'.",objectName));
			return false;
		}
		if(target.amWearingAt(Wearable.IN_INVENTORY))
		{
			mob.tell(L("You seem to be wearing or holding the item '@x1'.",objectName));
			return false;
		}
		String searchWhat=null;
		if(commands.size()>2)
		{
			final String s=commands.get(1);
			if(s.equalsIgnoreCase("room"))
				searchWhat="R";
			if(s.equalsIgnoreCase("area"))
				searchWhat="E";
			if(s.equalsIgnoreCase("mob"))
				searchWhat="M";
			if(s.equalsIgnoreCase("monster"))
				searchWhat="M";
			if(s.equalsIgnoreCase("player"))
				searchWhat="P";
			if(s.equalsIgnoreCase("user"))
				searchWhat="P";
			if(s.equalsIgnoreCase("item"))
				searchWhat="I";
			if(s.equalsIgnoreCase("object"))
				searchWhat="I";
			if(searchWhat!=null)
				commands.remove(1);
		}
		if(searchWhat==null)
			searchWhat="ERIPM";
		final String destinationString=CMParms.combine(commands,1).trim().toUpperCase();
		final List<Room> candidates=CMLib.map().findWorldRoomsLiberally(mob,destinationString,searchWhat,10,600000);
		if(candidates.size()==0)
		{
			mob.tell(L("You don't know of a place called '@x1'.",destinationString.toLowerCase()));
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
			newRoom=candidates.get(CMLib.dice().roll(1,candidates.size(),-1));
			if(((newRoom.roomID().length()==0)&&(CMLib.dice().rollPercentage()>50))
			||((newRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)&&(CMLib.dice().rollPercentage()>10)))
			{
				newRoom=null;
				continue;
			}
			final CMMsg enterMsg=CMClass.getMsg(mob,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
			if(!newRoom.okMessage(mob,enterMsg))
				newRoom=null;
			tries++;
		}

		if((newRoom==null)||(newRoom==oldRoom))
		{
			mob.tell(L("Your magic seems unable to send anything there."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),L("^S<S-NAME> invoke(s) a teleportation spell upon <T-NAME>.^?"));
		if(oldRoom.okMessage(mob,msg))
		{
			oldRoom.send(mob,msg);
			newRoom.bringMobHere(mob,false);
			target.unWear();
			success=CMLib.commands().postDrop(mob,target,true,false,false) && (!mob.isMine(target));
			oldRoom.bringMobHere(mob,false);
			if(success)
			{
				oldRoom.show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> vanishes!"));
				newRoom.showOthers(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> appear(s) out of nowhere!"));
			}
			else
				mob.tell(L("Nothing happens."));
		}
		// return whether it worked
		return success;
	}
}
