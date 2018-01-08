package com.planet_ink.coffee_mud.Behaviors;
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
public class ActiveTicker extends StdBehavior
{
	@Override
	public String ID()
	{
		return "ActiveTicker";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS|Behavior.CAN_EXITS|Behavior.CAN_AREAS;
	}

	protected int minTicks=10;
	protected int maxTicks=30;
	protected int chance=100;
	//protected short speed=1;
	protected int tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;

	protected void tickReset()
	{
		tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;
	}

	@Override
	public void setParms(String newParms)
	{
		parms=newParms;
		minTicks=CMParms.getParmInt(parms,"min",minTicks);
		maxTicks=CMParms.getParmInt(parms,"max",maxTicks);
		chance=CMParms.getParmInt(parms,"chance",chance);
		tickReset();
	}

	public String rebuildParms()
	{
		final StringBuffer rebuilt=new StringBuffer("");
		rebuilt.append(" min="+minTicks);
		rebuilt.append(" max="+maxTicks);
		rebuilt.append(" chance="+chance);
		return rebuilt.toString();
	}

	public String getParmsNoTicks()
	{
		String parms=getParms();
		char c=';';
		int x=parms.indexOf(c);
		if(x<0)
		{
			c='/';
			x=parms.indexOf(c);
		}
		if(x>0)
		{
			if((x+1)>parms.length())
				return "";
			parms=parms.substring(x+1);
		}
		else
		{
			return "";
		}
		return parms;
	}

	protected boolean canAct(Tickable ticking, int tickID)
	{
		switch(tickID)
		{
		case Tickable.TICKID_AREA:
		{
			if(!(ticking instanceof Area))
				break;
		}
		//$FALL-THROUGH$
		case Tickable.TICKID_MOB:
		case Tickable.TICKID_ITEM_BEHAVIOR:
		case Tickable.TICKID_ROOM_BEHAVIOR:
		{
			if((--tickDown)<1)
			{
				tickReset();
				if((ticking instanceof MOB)&&(!canActAtAll(ticking)))
					return false;
				final int a=CMLib.dice().rollPercentage();
				if(a>chance)
					return false;
				return true;
			}
			break;
		}
		default:
			break;
		}
		return false;
	}
}
