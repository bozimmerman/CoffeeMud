package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.EachApplicable.ApplyAffectPhyStats;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class StdRideableWrapper extends StdMobWrapper implements MOB, Rideable, CMObjectWrapper
{
	@Override
	public String ID()
	{
		return "StdRideableWrapper";
	}

	protected Rideable rideable = null;

	@Override
	public void setWrappedObject(final CMObject obj)
	{
		super.setWrappedObject(obj);
		if(obj instanceof Rideable)
		{
			rideable=(Rideable)obj;
		}
	}

	@Override
	public CMObject newInstance()
	{
		return new StdRideableWrapper();
	}

	@Override
	public boolean isMobileRideBasis()
	{
		return (rideable == null) ? false : rideable.isMobileRideBasis();
	}

	@Override
	public int rideBasis()
	{
		return (rideable == null) ? 0 : rideable.rideBasis();
	}

	@Override
	public void setRideBasis(final int basis)
	{
	}

	@Override
	public int riderCapacity()
	{
		return (rideable == null) ? 0 : rideable.riderCapacity();
	}

	@Override
	public void setRiderCapacity(final int newCapacity)
	{
	}

	@Override
	public int numRiders()
	{
		return (rideable == null) ? 0 : rideable.numRiders();
	}

	@Override
	public Enumeration<Rider> riders()
	{
		return (rideable == null) ? new EmptyEnumeration<Rider>() : rideable.riders();
	}

	@Override
	public Rider fetchRider(final int which)
	{
		return (rideable == null) ? null : rideable.fetchRider(which);
	}

	@Override
	public void addRider(final Rider mob)
	{
	}

	@Override
	public void delRider(final Rider mob)
	{
	}

	@Override
	public boolean amRiding(final Rider mob)
	{
		return (rideable == null) ? false : rideable.amRiding(mob);
	}

	@Override
	public String stateString(final Rider R)
	{
		return (rideable == null) ? "" : rideable.stateString(R);
	}

	@Override
	public String getStateString()
	{
		return (rideable == null) ? "" : rideable.getStateString();
	}

	@Override
	public void setStateString(final String str)
	{
	}

	@Override
	public String rideString(final Rider R)
	{
		return (rideable == null) ? "" : rideable.rideString(R);
	}

	@Override
	public String getRideString()
	{
		return (rideable == null) ? "" : rideable.getRideString();
	}

	@Override
	public void setRideString(final String str)
	{
	}

	@Override
	public String putString(final Rider R)
	{
		return (rideable == null) ? "" : rideable.putString(R);
	}

	@Override
	public String getPutString()
	{
		return (rideable == null) ? "" : rideable.getPutString();
	}

	@Override
	public void setPutString(final String str)
	{
	}

	@Override
	public String stateStringSubject(final Rider R)
	{
		return (rideable == null) ? "" : rideable.stateStringSubject(R);
	}

	@Override
	public String getStateStringSubject()
	{
		return (rideable == null) ? "" : rideable.getStateStringSubject();
	}

	@Override
	public void setStateStringSubject(final String str)
	{
	}

	@Override
	public boolean mobileRideBasis()
	{
		return (rideable == null) ? false : rideable.mobileRideBasis();
	}

	@Override
	public String mountString(final int commandType, final Rider R)
	{
		return (rideable == null) ? "" : rideable.mountString(commandType, R);
	}

	@Override
	public String getMountString()
	{
		return (rideable == null) ? "" : rideable.getMountString();
	}

	@Override
	public void setMountString(final String str)
	{
	}

	@Override
	public String dismountString(final Rider R)
	{
		return (rideable == null) ? "" : rideable.dismountString(R);
	}

	@Override
	public String getDismountString()
	{
		return (rideable == null) ? "" : rideable.getDismountString();
	}

	@Override
	public void setDismountString(final String str)
	{
	}

	@Override
	public Set<MOB> getRideBuddies(final Set<MOB> list)
	{
		return (rideable == null) ? list : rideable.getRideBuddies(list);
	}
}
