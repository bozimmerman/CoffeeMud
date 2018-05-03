package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.Conquerable;
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
   Copyright 2013-2018 Bo Zimmerman

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
public class Trap_ExitRoom extends Trap_Trap
{
	@Override
	public String ID()
	{
		return "Trap_ExitRoom";
	}

	private final static String	localizedName	= CMLib.lang().L("Exit Trap");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	public PairVector<MOB, Integer>	safeDirs	= new PairVector<MOB, Integer>();

	protected boolean mayNotLeave() { return true; }

	@Override 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CMObject copyOf()
	{
		final Trap_ExitRoom obj=(Trap_ExitRoom)super.copyOf();
		if(safeDirs == null)
			obj.safeDirs = null;
		else
			obj.safeDirs=(PairVector)safeDirs.clone();
		return obj;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(sprung)
			return super.okMessage(myHost,msg);
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.amITarget(affected)&& (affected instanceof Room) && (msg.tool() instanceof Exit))
		{
			final Room room=(Room)affected;
			if ((msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.targetMinor()==CMMsg.TYP_FLEE))
			{
				final int movingInDir=CMLib.map().getExitDir(room, (Exit)msg.tool());
				if((movingInDir!=Directions.DOWN)&&(movingInDir!=Directions.UP))
				{
					synchronized(safeDirs)
					{
						for(final Iterator<Pair<MOB,Integer>> i=safeDirs.iterator();i.hasNext();)
						{
							final Pair<MOB,Integer> p=i.next();
							if(p.first == msg.source())
							{
								i.remove();
								if(movingInDir==p.second.intValue())
									return true;
								spring(msg.source());
								return !mayNotLeave();
							}
						}
					}
				}
			}
			else
			if (msg.targetMinor()==CMMsg.TYP_ENTER)
			{
				final int movingInDir=CMLib.map().getExitDir((Room)affected, (Exit)msg.tool());
				if((movingInDir!=Directions.DOWN)&&(movingInDir!=Directions.UP))
				{
					synchronized(safeDirs)
					{
						final int dex=safeDirs.indexOf(msg.source());
						if(dex>=0)
							safeDirs.remove(dex);
						while(safeDirs.size()>room.numInhabitants()+1)
							safeDirs.remove(0);
						safeDirs.add(new Pair<MOB,Integer>(msg.source(),Integer.valueOf(movingInDir)));
					}
				}
			}
		}
		return true;
	}
}
