package com.planet_ink.coffee_mud.core.collections;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
/**
 * A tracking vector keeps track of which other tracking vectors that items of
 * the same type have been added to so that, if desired, items can be removed
 * from all vectors at once. The constructor requires a map to keep track of the
 * objects.
 * 
 * This class is required for RTree
 * 
 * @author Bo Zimmerman
 * 
 * @param <T> the type, or something?
 */
public class TrackingVector<T> extends Vector<T>
{
	private static final long									 serialVersionUID = 3331770309040710349L;

	private final Map<T, List<WeakReference<TrackingVector<T>>>> tracker;
	private final WeakReference<TrackingVector<T>>				 myRef			  = new WeakReference<TrackingVector<T>>(this);
	private final WeakReference<TrackBack<T>>					 trackBackRef;

	public interface TrackBack<T>
	{
		public void removed(T o);
	}

	public TrackingVector(Map<T, List<WeakReference<TrackingVector<T>>>> tracker)
	{
		super();
		this.tracker = tracker;
		trackBackRef = null;
	}

	public TrackingVector(Map<T, List<WeakReference<TrackingVector<T>>>> tracker, int sz)
	{
		super(sz);
		this.tracker = tracker;
		trackBackRef = null;
	}

	public TrackingVector(Map<T, List<WeakReference<TrackingVector<T>>>> tracker, TrackBack<T> obj)
	{
		super();
		this.tracker = tracker;
		this.trackBackRef = new WeakReference<TrackBack<T>>(obj);
	}

	public TrackingVector(Map<T, List<WeakReference<TrackingVector<T>>>> tracker, int sz, TrackBack<T> obj)
	{
		super(sz);
		this.tracker = tracker;
		this.trackBackRef = new WeakReference<TrackBack<T>>(obj);
	}

	protected void addTrackedEntry(T e)
	{
		synchronized (tracker)
		{
			if (tracker.containsKey(e))
			{
				final List<WeakReference<TrackingVector<T>>> l = tracker.get(e);
				if (!l.contains(myRef))
					l.add(myRef);
			}
			else
			{
				final List<WeakReference<TrackingVector<T>>> l = new Vector<WeakReference<TrackingVector<T>>>();
				l.add(myRef);
				tracker.put(e, l);
			}
		}
	}

	protected void removeTrackedEntry(Object e)
	{
		synchronized (tracker)
		{
			if (tracker.containsKey(e))
			{
				final List<WeakReference<TrackingVector<T>>> l = tracker.get(e);
				l.remove(myRef);
			}
		}
	}

	public void removeAllTrackedEntries(T e)
	{
		synchronized (tracker)
		{
			if (tracker.containsKey(e))
			{
				final List<WeakReference<TrackingVector<T>>> l = tracker.get(e);
				for (final WeakReference<TrackingVector<T>> ref : l)
				{
					if (ref.get() != null)
						ref.get().removeOnlyFromMe(e);
				}
				tracker.remove(e);
			}
		}
	}

	@Override
	public synchronized boolean add(T e)
	{
		if (super.add(e))
		{
			addTrackedEntry(e);
			return true;
		}
		return false;
	}

	@Override
	public synchronized void addElement(T e)
	{
		super.addElement(e);
		addTrackedEntry(e);
	}

	@Override
	public void add(int arg0, T arg1)
	{
		super.add(arg0, arg1);
		addTrackedEntry(arg1);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> arg0)
	{
		for (final T o : arg0)
			addTrackedEntry(o);
		return super.addAll(arg0);
	}

	@Override
	public synchronized boolean addAll(int arg0, Collection<? extends T> arg1)
	{
		for (final T o : arg1)
			addTrackedEntry(o);
		return super.addAll(arg0, arg1);
	}

	@Override
	public void clear()
	{
		for (final T e : this)
			removeTrackedEntry(e);
		super.clear();
	}

	@Override
	public boolean remove(Object arg0)
	{
		if (removeOnlyFromMe(arg0))
		{
			removeTrackedEntry(arg0);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean removeOnlyFromMe(Object arg0)
	{
		final boolean success = super.remove(arg0);
		if ((trackBackRef != null) && (trackBackRef.get() != null))
			trackBackRef.get().removed((T) arg0);
		return success;
	}

	@Override
	public synchronized T remove(int arg0)
	{
		final T x = super.remove(arg0);
		if (x != null)
		{
			removeTrackedEntry(x);
			if ((trackBackRef != null) && (trackBackRef.get() != null))
				trackBackRef.get().removed(x);
		}
		return x;
	}

	@Override
	public synchronized boolean removeAll(Collection<?> arg0)
	{
		for (final Object e : arg0)
			removeTrackedEntry(e);
		return super.removeAll(arg0);
	}
}
