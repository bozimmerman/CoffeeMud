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
public class WeakSHashSet<K> implements Serializable, Iterable<K>, Collection<K>, Set<K>
{
	private static final long				serialVersionUID	= -6713012858869312626L;
	private transient final ReentrantLock	lock				= new ReentrantLock();
	private volatile WeakHashMap<K,Boolean>	T;

	public WeakSHashSet()
	{
		T = new WeakHashMap<K,Boolean>();
	}

	public WeakSHashSet(final int x)
	{
		T = new WeakHashMap<K,Boolean>();
	}

	public WeakSHashSet(final List<K> V)
	{
		T = new WeakHashMap<K,Boolean>();
		if (V != null)
		{
			for (final K o : V)
				T.put(o, Boolean.TRUE);
		}
	}

	public WeakSHashSet(final K[] V)
	{
		T = new WeakHashMap<K,Boolean>();
		if (V != null)
		{
			for (final K o : V)
				T.put(o, Boolean.TRUE);
		}
	}

	public WeakSHashSet(final Enumeration<K> V)
	{
		T = new WeakHashMap<K,Boolean>();
		if (V != null)
		{
			for (; V.hasMoreElements();)
				T.put(V.nextElement(), Boolean.TRUE);
		}
	}

	public WeakSHashSet(final Iterator<K> V)
	{
		T = new WeakHashMap<K,Boolean>();
		if (V != null)
		{
			for (; V.hasNext();)
				T.put(V.next(), Boolean.TRUE);
		}
	}

	public WeakSHashSet(final Set<K> E)
	{
		T = new WeakHashMap<K,Boolean>();
		if (E != null)
		{
			for (final K o : E)
				T.put(o, Boolean.TRUE);
		}
	}

	public void addAll(final Enumeration<K> E)
	{
		if (E != null)
		{
			final ReentrantLock lock = this.lock;
			lock.lock();
			final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
			try
			{
				for (; E.hasMoreElements();)
					T2.put(E.nextElement(), Boolean.TRUE);
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
			final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
			try
			{
				for (; E.hasNext();)
					T2.put(E.next(), Boolean.TRUE);
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
			final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
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
			final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
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
			final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
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
		return new HashSet<K>(T.keySet());
	}

	public Vector<K> toVector()
	{
		final Vector<K> V = new Vector<K>(size());
		for (final K k : T.keySet())
			V.add(k);
		return V;
	}

	@Override
	public boolean add(final K e)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
		try
		{
			return T2.put(e, Boolean.TRUE) == null;
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	public boolean addUnsafe(final K e)
	{
		return T.put(e, Boolean.TRUE) == null;
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
		try
		{
			boolean found = false;
			for (final K o : c)
				found = (T2.put(o, Boolean.TRUE) != null) || found;
			return found;
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
			this.T = new WeakHashMap<K, Boolean>();
		}
		finally
		{
			lock.unlock();
		}
	}

	public WeakSHashSet<K> copyOf()
	{
		final WeakSHashSet<K> TS = new WeakSHashSet<K>();
		TS.T = new WeakHashMap<K, Boolean>(T);
		return TS;
	}

	@Override
	public boolean contains(final Object o)
	{
		return T.containsKey(o);
	}

	@Override
	public boolean isEmpty()
	{
		return T.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(T.keySet().iterator())
		{
			@Override
			public K next()
			{
				return super.next();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean remove(final Object o)
	{
		if(!T.containsKey(o))
			return false;
		final ReentrantLock lock = this.lock;
		lock.lock();
		final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
		try
		{
			return T2.remove(o) != null;
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
		final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>(T);
		try
		{
			boolean found = false;
			for (final Object o : arg0)
			{
				if (T2.containsKey(o))
					found = (T2.remove(o) != null) || found;
			}
			return found;
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
		for (final Object o : arg0)
		{
			if (!T.containsKey(o))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(final Collection<?> arg0)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final WeakHashMap<K,Boolean> T2 = new WeakHashMap<K,Boolean>();
		try
		{
			for (final Object o : arg0)
				T2.put((K)o, Boolean.TRUE);
			return true;
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
		return this.toVector().toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return this.toArray(arg0);
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

}
