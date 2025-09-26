package com.planet_ink.coffee_mud.core.collections;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/*
	Copyright 2016-2025 Bo Zimmerman

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
public class WeakArrayList<T> extends AbstractList<T>
{
	private final ArrayList<WeakReference<T>>	list;
	private final AtomicBoolean					needsCleaning	= new AtomicBoolean(false);
	private final AtomicLong					lastCleaning	= new AtomicLong(0);
	private static final long					cleanIntervalMs	= 30000;

	public WeakArrayList()
	{
		list = new ArrayList<WeakReference<T>>();
	}

	public WeakArrayList(final Collection<T> c)
	{
		list = new ArrayList<WeakReference<T>>();
		addAll(0, c);
	}

	@Override
	public synchronized boolean add(final T element)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		return list.add(new WeakReference<T>(element));
	}

	@Override
	public synchronized boolean remove(final Object element)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		for (int i = 0; i < list.size(); i++)
		{
			final WeakReference<T> W = list.get(i);
			if ((W != null) && (W.get() == element))
			{
				list.remove(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized T remove(final int index)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		return super.remove(index);
	}

	@Override
	public synchronized void add(final int index, final T element)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		list.add(index, new WeakReference<T>(element));
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized Iterator<T> iterator()
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		return new FilteredIterator<T>(new ConvertingIterator<WeakReference<T>,T>(list.iterator(), WeakConverter), WeakFilterer);
	}

	@Override
	public synchronized int size()
	{
		if(this.needsCleaning.get())
			cleanReleased();
		return list.size();
	}

	@Override
	public synchronized T get(final int index)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		return list.get(index).get();
	}

	private synchronized void cleanReleased()
	{
		for (final Iterator<WeakReference<T>> it = list.iterator(); it.hasNext();)
		{
			final WeakReference<T> ref =it.next();
			if (ref.get() == null)
				it.remove();
		}
		this.needsCleaning.set(false);
		this.lastCleaning.set(System.currentTimeMillis());
	}

	@SuppressWarnings("rawtypes")
	private final static Filterer WeakFilterer = new Filterer()
	{
		@Override
		public boolean passesFilter(final Object obj)
		{
			return (obj != null);
		}
	};

	private final Converter<WeakReference<T>, T> WeakConverter = new Converter<WeakReference<T>, T>()
	{
		@Override
		public T convert(final WeakReference<T> obj)
		{
			if(obj == null)
				return null;
			if(obj.get() == null)
			{
				needsCleaning.set(true);
			}
			return obj.get();
		}
	};
}