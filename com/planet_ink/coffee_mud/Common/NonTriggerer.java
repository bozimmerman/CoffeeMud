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
   Copyright 2022-2022 Bo Zimmerman

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
	public int compareTo(CMObject o)
	{
		return o == this?0:(o.hashCode()>=hashCode()?-1:1);
	}

	@Override
	public void addTrigger(Object key, String encodedTrigger, List<String> errors)
	{
	}

	@Override
	public boolean hasTrigger(Object key)
	{
		return false;
	}

	@Override
	public String getTriggerDesc(Object key)
	{
		return "";
	}

	@Override
	public CMMsg genNextAbleTrigger(MOB mob, Object key, boolean force)
	{
		return null;
	}

	@Override
	public void setIgnoreTracking(MOB mob, boolean truefalse)
	{
	}

	@Override
	public void deleteTracking(MOB mob, Object key)
	{
	}

	@Override
	public boolean isTracking(MOB mob, Object key)
	{
		return false;
	}

	@Override
	public boolean isTracking(Object key, CMMsg msg)
	{
		return false;
	}

	@Override
	public Object[] whichTracking(CMMsg msg)
	{
		return trackingNothing;
	}

	@Override
	public boolean isCompleted(Object key, CMMsg msg)
	{
		return false;
	}

	@Override
	public Object[] whichCompleted(Object[] keys, CMMsg msg)
	{
		return trackingNothing;
	}

	@Override
	public Pair<Object, List<String>> getCompleted(Object[] keys, CMMsg msg)
	{
		return null;
	}

	@Override
	public MOB[] whosDoneWaiting()
	{
		return trackingNoone;
	}

	@Override
	public Object[] getInProgress(MOB mob)
	{
		return trackingNothing;
	}

	@Override
	public boolean wasCompletedRecently(MOB mob, Object key)
	{
		return false;
	}
}
