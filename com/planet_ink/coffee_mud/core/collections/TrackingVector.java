package com.planet_ink.coffee_mud.core.collections;
import java.lang.ref.WeakReference;
import java.util.*;
/*
Copyright 2000-2013 Bo Zimmerman

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

public class TrackingVector<T> extends Vector<T>
{
	private static final long serialVersionUID = 3331770309040710349L;

	private final Map<T,List<WeakReference<TrackingVector<T>>>> tracker;
	private final WeakReference<TrackingVector<T>> myRef=new WeakReference<TrackingVector<T>>(this);
	
	public TrackingVector(Map<T,List<WeakReference<TrackingVector<T>>>> tracker)
	{
		super();
		this.tracker=tracker;
	}

	public TrackingVector(Map<T,List<WeakReference<TrackingVector<T>>>> tracker, int sz)
	{
		super(sz);
		this.tracker=tracker;
	}

	protected void addTrackedEntry(T e)
	{
		synchronized(tracker)
		{
			if(tracker.containsKey(e))
			{
				List<WeakReference<TrackingVector<T>>> l=tracker.get(e);
				if(!l.contains(myRef))
					l.add(myRef);
			}
			else
			{
				List<WeakReference<TrackingVector<T>>> l=new Vector<WeakReference<TrackingVector<T>>>();
				l.add(myRef);
				tracker.put(e, l);
			}
		}
	}
	
	protected void removeTrackedEntry(Object e)
	{
		synchronized(tracker)
		{
			if(tracker.containsKey(e))
			{
				List<WeakReference<TrackingVector<T>>> l=tracker.get(e);
				l.remove(myRef);
			}
		}
	}
	
	public void removeAllTrackedEntries(T e)
	{
		synchronized(tracker)
		{
			if(tracker.containsKey(e))
			{
				List<WeakReference<TrackingVector<T>>> l=tracker.get(e);
				for(WeakReference<TrackingVector<T>> ref : l)
					if(ref.get()!=null)
						ref.get().removeOnlyFromMe(e);
				tracker.remove(e);
			}
		}
	}
	
	@Override public boolean add(T e)
	{
		if(super.add(e))
		{
			addTrackedEntry(e);
			return true;
		}
		return false;
	}
	
	@Override public void addElement(T e)
	{
		super.addElement(e);
		addTrackedEntry(e);
	}
	
	@Override
	public void add(int arg0, T arg1) {
		super.add(arg0,arg1);
		addTrackedEntry(arg1);
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		for(T o : arg0)
			addTrackedEntry(o);
		return super.addAll(arg0);
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		for(T o : arg1)
			addTrackedEntry(o);
		return super.addAll(arg0, arg1);
	}

	@Override
	public void clear() {
		for(T e : this)
			removeTrackedEntry(e);
		super.clear();
	}
	
	
	@Override
	public boolean remove(Object arg0) {
		if(super.remove(arg0))
		{
			removeTrackedEntry(arg0);
			return true;
		}
		return false;
	}

	public boolean removeOnlyFromMe(Object arg0) {
		return super.remove(arg0);
	}

	@Override
	public T remove(int arg0) {
		T x=super.remove(arg0);
		if(x!=null)
			removeTrackedEntry(x);
		return x;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		for(Object e : arg0)
			removeTrackedEntry(e);
		return super.removeAll(arg0);
	}
}
