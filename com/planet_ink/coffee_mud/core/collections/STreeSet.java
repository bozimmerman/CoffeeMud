package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/*
   Copyright 2010-2021 Bo Zimmerman

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
	private static final long				serialVersionUID	= -6713012858869312626L;
	private transient final ReentrantLock	lock				= new ReentrantLock();
	private volatile TreeSet<K>				T;

	public STreeSet()
	{
		T = new TreeSet<K>();
	}

	public STreeSet(final Comparator<? super K> comp)
	{
		T = new TreeSet<K>(comp);
	}

	public STreeSet(final List<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			this.T.addAll(E);
		}
	}

	public STreeSet(final K[] E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (final K o : E)
				T.add(o);
		}
	}

	public STreeSet(final Enumeration<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (; E.hasMoreElements();)
				T.add(E.nextElement());
		}
	}

	public STreeSet(final Iterator<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (; E.hasNext();)
				T.add(E.next());
		}
	}

	public STreeSet(final Set<K> E)
	{
		T = new TreeSet<K>();
		if (E != null)
		{
			for (final K o : E)
				add(o);
		}
	}

	public void addAll(final Enumeration<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final TreeSet<K> T2 = new TreeSet<K>(T);
			try
			{
				for (; E.hasMoreElements();)
					T2.add(E.nextElement());
			}
			finally
			{
				this.T=T2;
				lock.unlock();
			}
		}
	}

	public void addAll(final K[] E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final TreeSet<K> T2 = new TreeSet<K>(T);
			try
			{
				for (final K e : E)
					T2.add(e);
			}
			finally
			{
				this.T=T2;
				lock.unlock();
			}
		}
	}

	public void addAll(final Iterator<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final TreeSet<K> T2 = new TreeSet<K>(T);
			try
			{
				for (; E.hasNext();)
					T2.add(E.next());
			}
			finally
			{
				this.T=T2;
				lock.unlock();
			}
		}
	}

	public void removeAll(final Enumeration<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final TreeSet<K> T2 = new TreeSet<K>(T);
			try
			{
				for (; E.hasMoreElements();)
					T2.remove(E.nextElement());
			}
			finally
			{
				this.T=T2;
				lock.unlock();
			}
		}
	}

	public void removeAll(final Iterator<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final TreeSet<K> T2 = new TreeSet<K>(T);
			try
			{
				for (; E.hasNext();)
					T2.remove(E.next());
			}
			finally
			{
				this.T=T2;
				lock.unlock();
			}
		}
	}

	public void removeAll(final List<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final TreeSet<K> T2 = new TreeSet<K>(T);
			try
			{
				for (final K o : E)
					T2.remove(o);
			}
			finally
			{
				this.T=T2;
				lock.unlock();
			}
		}
	}

	public TreeSet<K> toTreeSet()
	{
		return new TreeSet<K>(T);
	}

	public Vector<K> toVector()
	{
		final Vector<K> V = new Vector<K>(size());
		for (final K k : T)
			V.add(k);
		return V;
	}

	@Override
	public boolean add(final K e)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeSet<K> T2 = new TreeSet<K>(T);
		try
		{
			return T2.add(e);
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeSet<K> T2 = new TreeSet<K>(T);
		try
		{
			return T2.addAll(c);
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public K ceiling(final K e)
	{
		return T.ceiling(e);
	}

	@Override
	public void clear()
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		try
		{
			if(T.size()==0)
				return;
			this.T = new TreeSet<K>();
		}
		finally
		{
			lock.unlock();
		}
	}

	public STreeSet<K> copyOf()
	{
		final STreeSet<K> TS = new STreeSet<K>();
		TS.T = new TreeSet<K>(T);
		return TS;
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return T.comparator();
	}

	@Override
	public boolean contains(final Object o)
	{
		return T.contains(o);
	}

	@Override
	public Iterator<K> descendingIterator()
	{
		return new ReadOnlyIterator<K>(T.descendingIterator());
	}

	@Override
	public NavigableSet<K> descendingSet()
	{
		return T.descendingSet();
	}

	@Override
	public K first()
	{
		return T.first();
	}

	@Override
	public K floor(final K e)
	{
		return T.floor(e);
	}

	@Override
	public NavigableSet<K> headSet(final K toElement, final boolean inclusive)
	{
		return T.headSet(toElement, inclusive);
	}

	@Override
	public SortedSet<K> headSet(final K toElement)
	{
		return T.headSet(toElement);
	}

	@Override
	public K higher(final K e)
	{
		return T.higher(e);
	}

	@Override
	public boolean isEmpty()
	{
		return T.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(T.iterator());
	}

	@Override
	public K last()
	{
		return T.last();
	}

	@Override
	public K lower(final K e)
	{
		return T.lower(e);
	}

	@Override
	public K pollFirst()
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeSet<K> T2 = new TreeSet<K>(T);
		try
		{
			return T2.pollFirst();
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public K pollLast()
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeSet<K> T2 = new TreeSet<K>(T);
		try
		{
			return T2.pollLast();
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public boolean remove(final Object o)
	{
		if(!T.contains(o))
			return false;
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeSet<K> T2 = new TreeSet<K>(T);
		try
		{
			return T2.remove(o);
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public int size()
	{
		return T.size();
	}

	@Override
	public NavigableSet<K> subSet(final K fromElement, final boolean fromInclusive, final K toElement, final boolean toInclusive)
	{
		return T.subSet(fromElement, fromInclusive, toElement, toInclusive);
	}

	@Override
	public SortedSet<K> subSet(final K fromElement, final K toElement)
	{
		return T.subSet(fromElement, toElement);
	}

	@Override
	public NavigableSet<K> tailSet(final K fromElement, final boolean inclusive)
	{
		return T.tailSet(fromElement, inclusive);
	}

	@Override
	public SortedSet<K> tailSet(final K fromElement)
	{
		return T.tailSet(fromElement);
	}

	@Override
	public boolean equals(final Object arg0)
	{
		return this == arg0;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean removeAll(final Collection<?> arg0)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeSet<K> T2 = new TreeSet<K>(T);
		try
		{
			return T2.removeAll(arg0);
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		return T.containsAll(arg0);
	}

	@Override
	public boolean retainAll(final Collection<?> arg0)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeSet<K> T2 = new TreeSet<K>(T);
		try
		{
			return T2.retainAll(arg0);
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public Object[] toArray()
	{
		return T.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return T.toArray(arg0);
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

}
