package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2016-2018 Bo Zimmerman

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
public class Song_PirateShanty extends Song
{
	@Override
	public String ID()
	{
		return "Song_PirateShanty";
	}

	private final static String	localizedName	= CMLib.lang().L("Pirate Shanty");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	public Set<MOB> singers=new HashSet<MOB>();
	
	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.tool()!=null)
		&&(msg.tool().ID().equals(ID()))
		&&(!singers.contains(msg.source())))
		{
			synchronized(singers)
			{
				singers.add(msg.source());
			}
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!super.tick(ticking,tickID))||(!(affected instanceof MOB)))
			return false;
		
		final MOB M=(MOB)affected;
		if(M==null)
			return false;
		final Room R=CMLib.map().roomLocation(M);
		if(R==null)
			return false;
		int ct=0;
		synchronized(singers)
		{
			for(Iterator<MOB> i=singers.iterator();i.hasNext();)
			{
				if(i.next().location()!=R)
					i.remove();
			}
			ct=singers.size();
		}
		if(ct>0)
		{
			M.curState().adjMovement(ct*ct, M.maxState());
			M.curState().adjFatigue(-(ct*ct), M.maxState());
		}
		return true;
	}
}
