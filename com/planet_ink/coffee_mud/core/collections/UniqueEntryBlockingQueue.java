package com.planet_ink.coffee_mud.core.collections;

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
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public class UniqueEntryBlockingQueue<K> extends ArrayBlockingQueue<K>
{
	private static final long	serialVersionUID	= 3311623439390188911L;

	public UniqueEntryBlockingQueue(int capacity)
	{
		super(capacity);
	}

	public UniqueEntryBlockingQueue(int capacity, boolean fair)
	{
		super(capacity, fair);
	}

	public UniqueEntryBlockingQueue(int capacity, boolean fair, Collection<? extends K> c)
	{
		super(capacity, fair, c);
	}

	@Override
	public synchronized boolean offer(K e)
	{
		if (!contains(e))
			return super.offer(e);
		return true;
	}

	@Override
	public synchronized boolean offer(K e, long timeout, TimeUnit unit) throws InterruptedException
	{
		if (!contains(e))
			return super.offer(e, timeout, unit);
		return true;
	}

	@Override
	public synchronized void put(K e) throws InterruptedException
	{
		if (!contains(e))
			super.put(e);
	}

	@Override
	public synchronized boolean add(K e)
	{
		if (!contains(e))
			return super.add(e);
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends K> c)
	{
		if (c == null)
			return true;
		for (final K k : c)
		{
			if (!contains(k))
				add(k);
		}
		return true;
	}
}
