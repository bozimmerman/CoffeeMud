package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AccountStats.PrideStat;
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
public class QuestPoint extends StdItem
{
	@Override
	public String ID()
	{
		return "QuestPoint";
	}

	public QuestPoint()
	{
		super();
		setName("a quest point");
		setDisplayText("A shiny blue coin has been left here.");
		myContainer=null;
		setDescription("A shiny blue coin with magical script around the edges.");
		myUses=Integer.MAX_VALUE;
		myWornCode=0;
		material=0;
		basePhyStats.setWeight(0);
		basePhyStats.setSensesMask(basePhyStats().sensesMask()|PhyStats.SENSE_ITEMNORUIN|PhyStats.SENSE_ITEMNOWISH);
		recoverPhyStats();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
			{
				unWear();
				setContainer(null);
				if(!mob.isMine(this))
				{
					mob.setQuestPoint(mob.getQuestPoint()+1);
					CMLib.players().bumpPrideStat(mob,PrideStat.QUESTPOINTS_EARNED, 1);
				}
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
				destroy();
				return;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
}
