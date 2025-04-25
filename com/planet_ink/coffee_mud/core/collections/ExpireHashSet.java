package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2023-2025 Bo Zimmerman

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
/*
 * A Set that does not return items after an ellapsed period of time after being added.
 */
public class ExpireHashSet<K> implements Set<K>
{
	private final Map<K,Long> internalMap;
	private long expirationTime = 2 * 60000; // two minutes

	protected Long defTime()
	{
		return Long.valueOf(System.currentTimeMillis() + expirationTime);
	}


	public ExpireHashSet()
	{
		internalMap = new SHashtable<K,Long>();
	}

	public ExpireHashSet(final long expirationMs)
	{
		this();
		this.expirationTime = expirationMs;
	}

	public ExpireHashSet(final Set<K> s)
	{
		internalMap = new SHashtable<K,Long>();
		addAll(s);
	}

	@Override
	public boolean add(final K e)
	{
		internalMap.put(e, defTime());
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		for(final K k : c)
			add(k);
		return true;
	}

	@Override
	public void clear()
	{
		internalMap.clear();
	}

	@Override
	public boolean contains(final Object arg0)
	{
		final Long l = internalMap.get(arg0);
		if(l == null)
			return false;
		if(System.currentTimeMillis()<l.longValue())
			return true;
		internalMap.remove(arg0);
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		for(final Object o : arg0)
			if(!contains(o))
				return false;
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return internalMap.isEmpty();
	}

	protected Filterer<K> fconv = new Filterer<K>()
	{
		@Override
		public boolean passesFilter(final K obj)
		{
			if(obj == null)
				return false;
			final Long L = internalMap.get(obj);
			if(L == null)
				return false;
			if(System.currentTimeMillis()<L.longValue())
				return true;
			internalMap.remove(obj);
			return false;
		}
	};

	@Override
	public Iterator<K> iterator()
	{
		return new FilteredIterator<K>(internalMap.keySet().iterator(),fconv);
	}

	@Override
	public boolean remove(final Object o)
	{
		return internalMap.remove(o) != null;
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return internalMap.keySet().removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return internalMap.keySet().retainAll(c);
	}

	@Override
	public int size()
	{
		return internalMap.size();
	}

	@Override
	public Object[] toArray()
	{
		return internalMap.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return internalMap.keySet().toArray(arg0);
	}
}
