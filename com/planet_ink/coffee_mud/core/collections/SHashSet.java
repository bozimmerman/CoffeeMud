package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/*
   Copyright 2010-2025 Bo Zimmerman

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
	private static final long				serialVersionUID	= -6713012858869312626L;
	private transient final ReentrantLock	lock				= new ReentrantLock();
	private volatile HashSet<K>				T;

	public SHashSet()
	{
		T = new HashSet<K>();
	}

	public SHashSet(final int x)
	{
		T = new HashSet<K>(x);
	}

	public SHashSet(final List<K> V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (final K o : V)
				T.add(o);
		}
	}

	public SHashSet(final K[] V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (final K o : V)
				T.add(o);
		}
	}

	public SHashSet(final Enumeration<K> V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (; V.hasMoreElements();)
				T.add(V.nextElement());
		}
	}

	public SHashSet(final Iterator<K> V)
	{
		T = new HashSet<K>();
		if (V != null)
		{
			for (; V.hasNext();)
				T.add(V.next());
		}
	}

	public SHashSet(final Set<K> E)
	{
		T = new HashSet<K>();
		if (E != null)
		{
			for (final K o : E)
				T.add(o);
		}
	}

	public void addAll(final Enumeration<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final HashSet<K> T2 = new HashSet<K>(T);
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

	public void addAll(final Iterator<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final HashSet<K> T2 = new HashSet<K>(T);
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
			final HashSet<K> T2 = new HashSet<K>(T);
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
			final HashSet<K> T2 = new HashSet<K>(T);
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
			final HashSet<K> T2 = new HashSet<K>(T);
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

	public HashSet<K> toHashSet()
	{
		return new HashSet<K>(T);
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
		final HashSet<K> T2 = new HashSet<K>(T);
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

	public boolean addUnsafe(final K e)
	{
		return T.add(e);
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final HashSet<K> T2 = new HashSet<K>(T);
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
	public void clear()
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		try
		{
			if(T.size()==0)
				return;
			this.T = new HashSet<K>();
		}
		finally
		{
			lock.unlock();
		}
	}

	public SHashSet<K> copyOf()
	{
		final SHashSet<K> TS = new SHashSet<K>();
		TS.T = new HashSet<K>(T);
		return TS;
	}

	@Override
	public boolean contains(final Object o)
	{
		return T.contains(o);
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
	public boolean remove(final Object o)
	{
		if(!T.contains(o))
			return false;
		final ReentrantLock lock = this.lock;
		lock.lock();
		final HashSet<K> T2 = new HashSet<K>(T);
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
		final HashSet<K> T2 = new HashSet<K>(T);
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
		final HashSet<K> T2 = new HashSet<K>(T);
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
