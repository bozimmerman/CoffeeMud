package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Triggerer.TrigSignal;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Deity.RitualType;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.lang.ref.*;

/*
   Copyright 2022-2023 Bo Zimmerman

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
public class NonTriggerer implements Triggerer
{
	protected int version = -1;

	private final static Map<String, List<Social>> emptyMap = new ReadOnlyMap<String, List<Social>>(new HashMap<String, List<Social>>());
	private final static Object[]	trackingNothing	= new Object[0];
	private final static MOB[]		trackingNoone	= new MOB[0];

	public NonTriggerer()
	{
		version = TrigSignal.sig;
	}

	@Override
	public String ID()
	{
		return "NonTriggerer";
	}

	@Override
	public CMObject newInstance()
	{
		return new NonTriggerer();
	}

	@Override
	public String name()
	{
		return "";
	}

	@Override
	public Triggerer setName(final String name)
	{
		return this;
	}

	@Override
	public boolean isObsolete()
	{
		return version != TrigSignal.sig;
	}

	@Override
	public void setObsolete()
	{
		version = -1;
	}

	@Override
	public boolean isDisabled()
	{
		return true;
	}

	@Override
	public CMObject copyOf()
	{
		return this;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return o == this?0:(o.hashCode()>=hashCode()?-1:1);
	}

	@Override
	public void addTrigger(final Object key, final String encodedTrigger, Map<String, List<Social>> socials, final List<String> errors)
	{
	}

	@Override
	public boolean hasTrigger(final Object key)
	{
		return false;
	}

	@Override
	public String getTriggerDesc(final Object key)
	{
		return "";
	}

	@Override
	public CMMsg genNextAbleTrigger(final MOB mob, final Object key, final boolean force)
	{
		return null;
	}

	@Override
	public void setIgnoreTracking(final MOB mob, final boolean truefalse)
	{
	}

	@Override
	public void deleteTracking(final MOB mob, final Object key)
	{
	}

	@Override
	public boolean isTracking(final MOB mob, final Object key)
	{
		return false;
	}

	@Override
	public boolean isTracking(final Object key, final CMMsg msg)
	{
		return false;
	}

	@Override
	public Object[] whichTracking(final CMMsg msg)
	{
		return trackingNothing;
	}

	@Override
	public boolean isCompleted(final Object key, final CMMsg msg)
	{
		return false;
	}

	@Override
	public Object[] whichCompleted(final Object[] keys, final CMMsg msg)
	{
		return trackingNothing;
	}

	@Override
	public Pair<Object, List<String>> getCompleted(final Object[] keys, final CMMsg msg)
	{
		return null;
	}

	@Override
	public MOB[] whosDoneWaiting()
	{
		return trackingNoone;
	}

	@Override
	public Object[] getInProgress(final MOB mob)
	{
		return trackingNothing;
	}

	@Override
	public boolean wasCompletedRecently(final MOB mob, final Object key)
	{
		return false;
	}

	@Override
	public Map<String,List<Social>> getSocialSets()
	{
		return emptyMap;
	}
}
