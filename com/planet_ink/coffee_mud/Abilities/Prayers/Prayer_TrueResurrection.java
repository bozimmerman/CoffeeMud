package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class Prayer_TrueResurrection extends Prayer_Resurrect
{
	@Override
	public String ID()
	{
		return "Prayer_TrueResurrection";
	}

	private final static String localizedName = CMLib.lang().L("True Resurrection");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_DEATHLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	public Item findCorpseRoom(List<Item> candidates)
	{
		for(int m=0;m<candidates.size();m++)
		{
			final Item item = candidates.get(m);
			if(item instanceof DeadBody)
			{
				Room newRoom=CMLib.map().roomLocation(item);
				if(newRoom != null)
					return item;
			}
		}
		return null;
	}
	
	@Override
	public boolean supportsMending(Physical item)
	{
		if (item instanceof DeadBody)
		{
			final DeadBody body=(DeadBody)item;
			if((body.getMobName()==null)||(body.getMobName().length()==0))
				return false;
			return true;
		}
		return false;
	}

	@Override
	protected boolean canResurrectNormalMobs() 
	{ 
		return true; 
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(givenTarget == null)
		{
			if(commands.size()<1)
			{
				mob.tell(L("You must specify the name of a corpse within range of this magic."));
				return false;
			}
			final String corpseName=CMParms.combine(commands,0).trim().toUpperCase();

			List<Item> candidates=CMLib.map().findRoomItems(mob.location().getArea().getProperMap(), mob, corpseName, false, 5);
			Item corpseItem=this.findCorpseRoom(candidates);
			Room newRoom = null;
			if(corpseItem != null)
				newRoom=CMLib.map().roomLocation(corpseItem);
			if(newRoom == null)
			{
				candidates=CMLib.map().findRoomItems(CMLib.map().rooms(), mob, corpseName, false, 5);
				corpseItem=this.findCorpseRoom(candidates);
				if(corpseItem != null)
					newRoom=CMLib.map().roomLocation(corpseItem);
			}
			candidates.clear();
			if(newRoom==null)
			{
				mob.tell(L("You can't seem to fixate on a corpse called '@x1', perhaps it has decayed?",corpseName));
				return false;
			}
			givenTarget = corpseItem;
		}
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}
}
