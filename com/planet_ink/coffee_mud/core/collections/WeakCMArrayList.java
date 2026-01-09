package com.planet_ink.coffee_mud.core.collections;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;

/*
	Copyright 2025-2026 Bo Zimmerman

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
public class WeakCMArrayList<T> extends WeakArrayList<T>
{
	public WeakCMArrayList()
	{
		super();
	}

	public WeakCMArrayList(final Collection<T> c)
	{
		super(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized Iterator<T> iterator()
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		return new FilteredIterator<T>(new ConvertingIterator<WeakReference<T>,T>(list.iterator(), WeakConverter), WeakCMFilterer);
	}

	@SuppressWarnings("rawtypes")
	private final static Filterer WeakCMFilterer = new Filterer()
	{
		@Override
		public boolean passesFilter(Object obj)
		{
			if(obj instanceof Reference)
				obj = ((Reference)obj).get();
			if (obj instanceof Environmental)
				return !((Environmental) obj).amDestroyed();
			return (obj != null);
		}
	};
}