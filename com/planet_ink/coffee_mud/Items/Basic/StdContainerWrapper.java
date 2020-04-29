package com.planet_ink.coffee_mud.Items.Basic;
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
public class StdContainerWrapper extends StdItemWrapper implements Item, Container, CMObjectWrapper
{
	@Override
	public String ID()
	{
		return "StdContainerWrapper";
	}

	protected Container container = null;

	@Override
	public void setWrappedObject(final CMObject obj)
	{
		super.setWrappedObject(obj);
		if(obj instanceof Container)
		{
			container=(Container)obj;
		}
	}

	@Override
	public CMObject newInstance()
	{
		return new StdContainerWrapper();
	}

	@Override
	public boolean isOpen()
	{
		return (container == null) ? false : container.isOpen();
	}

	@Override
	public boolean isLocked()
	{
		return (container == null) ? false : container.isLocked();
	}

	@Override
	public boolean hasADoor()
	{
		return (container == null) ? false : container.hasADoor();
	}

	@Override
	public boolean hasALock()
	{
		return (container == null) ? false : container.hasALock();
	}

	@Override
	public boolean defaultsLocked()
	{
		return (container == null) ? false : container.defaultsLocked();
	}

	@Override
	public boolean defaultsClosed()
	{
		return (container == null) ? false : container.defaultsClosed();
	}

	@Override
	public void setDoorsNLocks(final boolean hasADoor, final boolean isOpen, final boolean defaultsClosed, final boolean hasALock, final boolean isLocked, final boolean defaultsLocked)
	{
	}

	@Override
	public String keyName()
	{
		return (container == null) ? "" : container.keyName();
	}

	@Override
	public void setKeyName(final String keyName)
	{
	}

	@Override
	public int openDelayTicks()
	{
		return (container == null) ? 0 :container.openDelayTicks();
	}

	@Override
	public void setOpenDelayTicks(final int numTicks)
	{
	}

	@Override
	public ReadOnlyList<Item> getDeepContents()
	{
		final List<Item> V=new Vector<Item>();
		if(owner()!=null)
		{
			Item I;
			for(final Enumeration<Item> e = owner().items(); e.hasMoreElements();)
			{
				I=e.nextElement();
				if(I!=null)
				{
					if(isInside(I))
						V.add(I);
				}
			}
		}
		return new ReadOnlyList<Item>(V);
	}

	@Override
	public ReadOnlyList<Item> getContents()
	{
		final List<Item> V=new ArrayList<Item>();
		if(owner()!=null)
		{
			Item I;
			for(final Enumeration<Item> e = owner().items(); e.hasMoreElements();)
			{
				I=e.nextElement();
				if((I!=null)&&(I.container()==this))
					V.add(I);
			}
		}
		return new ReadOnlyList<Item>(V);
	}

	@Override
	public int capacity()
	{
		return (container == null) ? 0 :container.capacity();
	}

	@Override
	public void setCapacity(final int newValue)
	{
	}

	@Override
	public boolean hasContent()
	{
		if(owner()!=null)
		{
			Item I;
			for(final Enumeration<Item> e = owner().items(); e.hasMoreElements();)
			{
				I=e.nextElement();
				if((I!=null)&&(I.container()==this))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean canContain(final Item I)
	{
		return (container == null) ? false :container.canContain(I);
	}

	@Override
	public boolean isInside(final Item I)
	{
		if(I==null)
			return false;
		if((I.container()==null)
		||(I.container()==I))
			return false;
		if(I.container()==this)
			return true;
		return isInside(I.container());
	}

	@Override
	public long containTypes()
	{
		return (container == null) ? 0 :container.containTypes();
	}

	@Override
	public void setContainTypes(final long containTypes)
	{
	}

	@Override
	public void emptyPlease(final boolean flatten)
	{
		final ItemPossessor C=owner();
		if(C!=null)
		{
			Item I;
			if(flatten)
			{
				final List<Item> V=getDeepContents();
				for(int v=0;v<V.size();v++)
				{
					I=V.get(v);
					I.setContainer(null);
				}
			}
			else
			for(final Enumeration<Item> e = C.items(); e.hasMoreElements();)
			{
				I=e.nextElement();
				if(I==null)
					continue;
				if(I.container()==this)
					I.setContainer(null);
			}
		}
	}
}
