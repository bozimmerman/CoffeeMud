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

public class STreeSet<K> implements Serializable, Iterable<K>, Collection<K>, NavigableSet<K>, Set<K>, SortedSet<K>
{
	private static final long	serialVersionUID	= -6713012858869312626L;
	private volatile TreeSet<K>	T;

	public STreeSet()
	{
		T = new TreeSet<K>();
	}

	public STreeSet(Comparator<K> comp)
	{
		T = new TreeSet<K>(comp);
	}

	public STreeSet(List<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			this.T.addAll(E);
		}
	}

	public STreeSet(K[] E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (final K o : E)
				T.add(o);
		}
	}

	public STreeSet(Enumeration<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (; E.hasMoreElements();)
				T.add(E.nextElement());
		}
	}

	public STreeSet(Iterator<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (; E.hasNext();)
				T.add(E.next());
		}
	}

	public STreeSet(Set<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (final K o : E)
				add(o);
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

	public synchronized void addAll(K[] E)
	{
		if (E != null)
		{
			for (final K e : E)
				T.add(e);
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
	public synchronized TreeSet<K> toTreeSet()
	{
		return (TreeSet<K>) T.clone();
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
		T = (TreeSet<K>) T.clone();
		return T.add(e);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean addAll(Collection<? extends K> c)
	{
		T = (TreeSet<K>) T.clone();
		return T.addAll(c);
	}

	@Override
	public synchronized K ceiling(K e)
	{
		return T.ceiling(e);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void clear()
	{
		T = (TreeSet<K>) T.clone();
		T.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized STreeSet<K> copyOf()
	{
		final STreeSet<K> TS = new STreeSet<K>();
		TS.T = (TreeSet<K>) T.clone();
		return TS;
	}

	@Override
	public synchronized Comparator<? super K> comparator()
	{
		return T.comparator();
	}

	@Override
	public synchronized boolean contains(Object o)
	{
		return T.contains(o);
	}

	@Override
	public synchronized Iterator<K> descendingIterator()
	{
		return new ReadOnlyIterator<K>(T.descendingIterator());
	}

	@Override
	public synchronized NavigableSet<K> descendingSet()
	{
		return T.descendingSet();
	}

	@Override
	public synchronized K first()
	{
		return T.first();
	}

	@Override
	public synchronized K floor(K e)
	{
		return T.floor(e);
	}

	@Override
	public synchronized NavigableSet<K> headSet(K toElement, boolean inclusive)
	{
		return T.headSet(toElement, inclusive);
	}

	@Override
	public synchronized SortedSet<K> headSet(K toElement)
	{
		return T.headSet(toElement);
	}

	@Override
	public synchronized K higher(K e)
	{
		return T.higher(e);
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

	@Override
	public synchronized K last()
	{
		return T.last();
	}

	@Override
	public synchronized K lower(K e)
	{
		return T.lower(e);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized K pollFirst()
	{
		T = (TreeSet<K>) T.clone();
		return T.pollFirst();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized K pollLast()
	{
		T = (TreeSet<K>) T.clone();
		return T.pollLast();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean remove(Object o)
	{
		T = (TreeSet<K>) T.clone();
		return T.remove(o);
	}

	@Override
	public synchronized int size()
	{
		return T.size();
	}

	@Override
	public synchronized NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive)
	{
		return T.subSet(fromElement, fromInclusive, toElement, toInclusive);
	}

	@Override
	public synchronized SortedSet<K> subSet(K fromElement, K toElement)
	{
		return T.subSet(fromElement, toElement);
	}

	@Override
	public synchronized NavigableSet<K> tailSet(K fromElement, boolean inclusive)
	{
		return T.tailSet(fromElement, inclusive);
	}

	@Override
	public synchronized SortedSet<K> tailSet(K fromElement)
	{
		return T.tailSet(fromElement);
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
		T = (TreeSet<K>) T.clone();
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
		T = (TreeSet<K>) T.clone();
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
