package com.planet_ink.coffee_mud.Areas;
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
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2008-2025 Bo Zimmerman

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
public class SubThinInstance extends StdThinInstance implements SubArea
{
	@Override
	public String ID()
	{
		return "SubThinInstance";
	}

	public SubThinInstance()
	{
		super.flags = Area.FLAG_THIN | Area.FLAG_INSTANCE_CHILD;
	}

	@Override
	protected boolean qualifiesToBeParentArea(final Area parentA)
	{
		return (parentA != this);
	}

	@Override
	protected boolean doesManageChildAreas()
	{
		return true;
	}

	@Override
	protected boolean doesManageMobLists()
	{
		return true;
	}

	@Override
	public Area getSuperArea()
	{
		if((parentArea!=null)&&(parentArea.get()!=null))
			return parentArea.get();
		final Area A=super.getSuperArea();
		if(A!=null)
			return A;
		int x=Name().indexOf('_');
		if(x<0)
			x=Name().indexOf(' ');
		if(x<0)
			return null;
		final Area parentA = CMLib.map().getArea(Name().substring(x+1));
		if((parentA==null)
		||(!qualifiesToBeParentArea(parentA)))
			return null;
		parentArea=new WeakReference<Area>(parentA);
		return parentA;
	}

	@Override
	protected AreaIStats getAreaIStats()
	{
		if(!CMProps.isState(CMProps.HostState.RUNNING))
			return emptyStats;
		AreaIStats statData=(AreaIStats)Resources.getResource("STATS_"+Name().toUpperCase());
		if(statData!=null)
			return statData;
		final Area parentArea=getSuperArea();
		final String areaName = (parentArea==null)?Name():parentArea.Name();
		statData=(AreaIStats)Resources.getResource("STATS_"+areaName.toUpperCase());
		if(statData!=null)
			return statData;
		if((parentArea!=null)
		&&(parentArea!=this))
		{
			parentArea.getIStat(Stats.AVG_ALIGNMENT); // force a build
			statData=(AreaIStats)Resources.getResource("STATS_"+parentArea.Name().toUpperCase());
			if(statData!=null)
				return statData;
		}
		return super.getAreaIStats();
	}

	@Override
	protected Area createRedirectArea(final List<MOB> mobs)
	{
		if(instanceChildren.size()==0)
		{
			final Area oldArea = this.getSuperArea();
			if(oldArea == null)
				return null;
			properRooms=new STreeMap<String, Room>(new Area.RoomIDComparator());
			properRoomIDSet = null;
			metroRoomIDSet = null;
			blurbFlags=new STreeMap<String,String>();
			for(final Enumeration<String> e=oldArea.getProperRoomnumbers().getRoomIDs();e.hasMoreElements();)
				addProperRoomnumber(convertToMyArea(e.nextElement()));
			setAreaState(Area.State.ACTIVE); // starts ticking
			final List<WeakReference<MOB>> newMobList = new SVector<WeakReference<MOB>>(5);
			for(final MOB mob : mobs)
				newMobList.add(new WeakReference<MOB>(mob));
			final AreaInstanceChild child = new AreaInstanceChild(this,newMobList);
			instanceChildren.add(child);
		}
		else
		{
			for(final MOB mob : mobs)
				instanceChildren.get(0).mobs.add(new WeakReference<MOB>(mob));
		}
		return this;
	}
}
