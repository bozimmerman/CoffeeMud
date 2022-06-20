package com.planet_ink.coffee_mud.core.collections;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
public class WeakLinkSetList<T> implements List<T>
{
	private final LinkedList<WeakReference<T>>	list;
	private final AtomicBoolean					needsCleaning	= new AtomicBoolean(false);
	private final AtomicLong					lastCleaning	= new AtomicLong(0);
	private static final long					cleanIntervalMs	= 30000;

	private static final long serialVersionUID = 2104498926040660576L;
	private final TreeMap<Integer,List<WeakReference<T>>> set;

	public WeakLinkSetList()
	{
		list = new LinkedList<WeakReference<T>>();
		set  = new TreeMap<Integer,List<WeakReference<T>>>();
	}

	public WeakLinkSetList(final Collection<T> c)
	{
		list = new LinkedList<WeakReference<T>>();
		set  = new TreeMap<Integer,List<WeakReference<T>>>();
		addAll(0, c);
	}

	@Override
	public synchronized boolean add(final T element)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		if(element != null)
		{
			final Integer setL = Integer.valueOf(element.hashCode());
			final WeakReference<T> ref = new WeakReference<T>(element);
			if(!set.containsKey(setL))
				set.put(setL, new LinkedList<WeakReference<T>>());
			set.get(setL).add(ref);
			return list.add(ref);
		}
		return false;
	}

	@Override
	public synchronized boolean remove(final Object element)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		if(element instanceof WeakReference)
		{
			@SuppressWarnings("unchecked")
			final WeakReference<T> ref = (WeakReference<T>)element;
			final T elemT = ref.get();
			if(elemT==null)
				return list.remove(element);
			final Integer setL = Integer.valueOf(elemT.hashCode());
			if(set.containsKey(setL))
			{
				final List<WeakReference<T>> lst = set.get(setL);
				lst.remove(ref);
				if(lst.size()==0)
					set.remove(setL);
			}
			return list.remove(ref);

		}
		else
		if(element != null)
		{
			final Integer setL = Integer.valueOf(element.hashCode());
			if(!set.containsKey(setL))
				return false;
			WeakReference<T> ref = null;
			final List<WeakReference<T>> lst = set.get(setL);
			for(final Iterator<WeakReference<T>> i=lst.iterator();i.hasNext();)
			{
				final WeakReference<T> t = i.next();
				if(t.get()==element)
				{
					i.remove();
					ref=t;
					break;
				}
			}
			if(lst.size()==0)
				set.remove(setL);
			if(ref != null)
				return list.remove(ref);
			return false;
		}
		return false;
	}

	@Override
	public synchronized T remove(final int index)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public synchronized void add(final int index, final T element)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		throw new java.lang.IllegalArgumentException();
	}

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
	public synchronized boolean contains(final Object element)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		if(element instanceof WeakReference)
		{
			@SuppressWarnings("unchecked")
			final WeakReference<T> ref = (WeakReference<T>)element;
			final T elemT = ref.get();
			if(elemT==null)
				return false;
			final Integer setL = Integer.valueOf(elemT.hashCode());
			if(set.containsKey(setL))
			{
				final List<WeakReference<T>> lst = set.get(setL);
				return lst.contains(ref);
			}
			return false;
		}
		else
		if(element != null)
		{
			final Integer setL = Integer.valueOf(element.hashCode());
			if(!set.containsKey(setL))
				return false;
			final List<WeakReference<T>> lst = set.get(setL);
			for(final Iterator<WeakReference<T>> i=lst.iterator();i.hasNext();)
			{
				final WeakReference<T> t = i.next();
				if(t.get()==element)
					return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public synchronized T get(final int index)
	{
		if(this.needsCleaning.get() && ((System.currentTimeMillis() - this.lastCleaning.get()) > cleanIntervalMs))
			cleanReleased();
		throw new java.lang.IllegalArgumentException();
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

	private final Filterer<T> WeakFilterer = new Filterer<T>()
	{
		@Override
		public boolean passesFilter(final T obj)
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

	@Override
	public boolean isEmpty()
	{
		return size()==0;
	}

	@Override
	public synchronized Object[] toArray()
	{
		final List<Object> objs = new ArrayList<Object>(size());
		for(final WeakReference<T> wt : list)
		{
			final T t = wt.get();
			if(t != null)
				objs.add(t);
		}
		return objs.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K> K[] toArray(final K[] a)
	{
		final Object[] objs = toArray();
		if(objs.length==a.length)
		{
			for(int i=0;i<a.length;i++)
				a[i] = (K)objs[i];
			return a;
		}
		return (K[])objs;
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		if(c==null)
			return false;
		for(final Object o : c)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends T> c)
	{
		if(c==null)
			return false;
		boolean res = true;
		for(final T o : c)
			res = this.add(o) && res;
		return res;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		if(c==null)
			return false;
		boolean res = true;
		for(final Object o : c)
			res = this.remove(o) && res;
		return res;
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		if(c==null)
			return false;
		if(size()==0)
			return true;
		boolean res = true;
		final LinkedList<WeakReference<T>> cpy = new LinkedList<WeakReference<T>>();
		cpy.addAll(list);
		for(final WeakReference<T> wt : cpy)
		{
			if(!c.contains(wt) && !c.contains(wt.get()))
				res = remove(wt) && res;
		}
		return res;
	}

	@Override
	public synchronized void clear()
	{
		list.clear();
		set.clear();
	}

	@Override
	public T set(final int index, final T element)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int indexOf(final Object o)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int lastIndexOf(final Object o)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public ListIterator<T> listIterator()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public ListIterator<T> listIterator(final int index)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex)
	{
		throw new java.lang.IllegalArgumentException();
	}
}