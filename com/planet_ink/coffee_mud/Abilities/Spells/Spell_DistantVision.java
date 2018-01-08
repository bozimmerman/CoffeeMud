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

public class Spell_DistantVision extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DistantVision";
	}

	private final static String localizedName = CMLib.lang().L("Distant Vision");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("Divine a vision of where?"));
			return false;
		}
		final String areaName=CMParms.combine(commands,0).trim().toUpperCase();
		Room thisRoom=null;
		try
		{
			final List<Room> rooms=CMLib.map().findRooms(CMLib.map().rooms(), mob, areaName, true, 10);
			if(rooms.size()>0)
				thisRoom=rooms.get(CMLib.dice().roll(1,rooms.size(),-1));
		}
		catch(final NoSuchElementException nse)
		{
		}

		if(thisRoom==null)
		{
			mob.tell(L("You can't seem to fixate on a place called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,thisRoom,auto),auto?"":L("^S<S-NAME> close(s) <S-HIS-HER> eyes, and invoke(s) a vision.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.tell(L("\n\r\n\r"));
				final CMMsg msg2=CMClass.getMsg(mob,thisRoom,CMMsg.MSG_LOOK,null);
				thisRoom.executeMsg(mob,msg2);
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> close(s) <S-HIS-HER> eyes, incanting, but then open(s) them in frustration."));

		// return whether it worked
		return success;
	}
}
