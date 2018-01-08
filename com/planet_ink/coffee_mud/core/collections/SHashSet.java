package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

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

public class SHashSet<K> implements Serializable, Iterable<K>, Collection<K>, Set<K>
{
	private static final long	serialVersionUID	= -6713012858869312626L;
	private volatile HashSet<K>	T;

	public SHashSet()
	{
		T = new HashSet<K>();
	}

	public SHashSet(int x)
	{
		T = new HashSet<K>(x);
	}

	public SHashSet(List<K> V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (final K o : V)
				T.add(o);
		}
	}

	public SHashSet(K[] V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (final K o : V)
				T.add(o);
		}
	}

	public SHashSet(Enumeration<K> V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (; V.hasMoreElements();)
				T.add(V.nextElement());
		}
	}

	public SHashSet(Iterator<K> V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (; V.hasNext();)
				T.add(V.next());
		}
	}

	public SHashSet(Set<K> E)
	{
		T = new HashSet<K>();
		if (E != null)
		{
			for (final K o : E)
				T.add(o);
		}
	}

	public synchronized void addAll(Enumeration<K> E)
	{
		if (E != null)
		{
			for (; E.hasMoreElements();)
				T.add(E.nextElement());
		}
	}

	public synchronized void addAll(Iterator<K> E)
	{
		if (E != null)
		{
			for (; E.hasNext();)
				T.add(E.next());
		}
	}

	public synchronized void removeAll(Enumeration<K> E)
	{
		if (E != null)
		{
			for (; E.hasMoreElements();)
				T.remove(E.nextElement());
		}
	}

	public synchronized void removeAll(Iterator<K> E)
	{
		if (E != null)
		{
			for (; E.hasNext();)
				T.remove(E.next());
		}
	}

	public synchronized void removeAll(List<K> E)
	{
		if (E != null)
		{
			for (final K o : E)
				T.remove(o);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized HashSet<K> toHashSet()
	{
		return (HashSet<K>) T.clone();
	}

	public synchronized Vector<K> toVector()
	{
		final Vector<K> V = new Vector<K>(size());
		for (final K k : T)
			V.add(k);
		return V;
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean add(K e)
	{
		T = (HashSet<K>) T.clone();
		return T.add(e);
	}

	public synchronized boolean addUnsafe(K e)
	{
		return T.add(e);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean addAll(Collection<? extends K> c)
	{
		T = (HashSet<K>) T.clone();
		return T.addAll(c);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void clear()
	{
		T = (HashSet<K>) T.clone();
		T.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized SHashSet<K> copyOf()
	{
		final SHashSet<K> TS = new SHashSet<K>();
		TS.T = (HashSet<K>) T.clone();
		return TS;
	}

	@Override
	public synchronized boolean contains(Object o)
	{
		return T.contains(o);
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return T.isEmpty();
	}

	@Override
	public synchronized Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(T.iterator());
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean remove(Object o)
	{
		T = (HashSet<K>) T.clone();
		return T.remove(o);
	}

	@Override
	public synchronized int size()
	{
		return T.size();
	}

	@Override
	public boolean equals(Object arg0)
	{
		return this == arg0;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean removeAll(Collection<?> arg0)
	{
		T = (HashSet<K>) T.clone();
		return T.removeAll(arg0);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> arg0)
	{
		return T.containsAll(arg0);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean retainAll(Collection<?> arg0)
	{
		T = (HashSet<K>) T.clone();
		return T.retainAll(arg0);
	}

	@Override
	public synchronized Object[] toArray()
	{
		return T.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] arg0)
	{
		return T.toArray(arg0);
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

}
