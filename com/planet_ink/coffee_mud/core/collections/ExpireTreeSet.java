package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2023-2024 Bo Zimmerman

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
public class ExpireTreeSet<K> implements Set<K>
{
	private final Map<K,Long> internalMap;
	private long expirationTime = 2 * 60000; // two minutes
	private volatile long nextExpirationTime = Long.MAX_VALUE;

	protected Long defTime()
	{
		return Long.valueOf(System.currentTimeMillis() + expirationTime);
	}

	public ExpireTreeSet()
	{
		internalMap = new STreeMap<K,Long>();
	}

	@SuppressWarnings("unchecked")
	public ExpireTreeSet(final long expirationMs)
	{
		internalMap = new STreeMap<K,Long>((Comparator<K>)XTreeSet.comparator);
		this.expirationTime = expirationMs;
	}

	@SuppressWarnings("unchecked")
	public ExpireTreeSet(final Set<K> s)
	{
		internalMap = new STreeMap<K,Long>((Comparator<K>)XTreeSet.comparator);
		addAll(s);
	}

	protected synchronized void expirationCheck()
	{
		if(System.currentTimeMillis() > nextExpirationTime)
		{
			final long now = System.currentTimeMillis();
			nextExpirationTime = Long.MAX_VALUE;
			for(final Iterator<Map.Entry<K,Long>> i = internalMap.entrySet().iterator();i.hasNext();)
			{
				final Map.Entry<K,Long> L = i.next();
				final long expiration = L.getValue().longValue();
				if(now > expiration)
					internalMap.remove(L.getKey());
				else
				if(expiration < nextExpirationTime)
					nextExpirationTime = expiration;
			}
		}
	}

	@Override
	public boolean add(final K e)
	{
		final Long time = defTime();
		if(time.longValue() < nextExpirationTime)
			nextExpirationTime = time.longValue();
		else
			expirationCheck();
		internalMap.put(e, time);
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		for(final K k : c)
			add(k);
		return true;
	}

	public boolean add(final K e, final long expirationMs)
	{
		final Long time = Long.valueOf(System.currentTimeMillis()+expirationMs);
		if(time.longValue() < nextExpirationTime)
			nextExpirationTime = time.longValue();
		else
			expirationCheck();
		internalMap.put(e, time);
		return true;
	}

	public boolean addAll(final Collection<? extends K> c, final long expirationMs)
	{
		for(final K k : c)
			add(k, expirationMs);
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
		expirationCheck();
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
			return false;
		}
	};

	@Override
	public Iterator<K> iterator()
	{
		expirationCheck();
		return new FilteredIterator<K>(internalMap.keySet().iterator(),fconv,true);
	}

	@Override
	public boolean remove(final Object o)
	{
		expirationCheck();
		return internalMap.remove(o) != null;
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		boolean success = true;
		for(final Object o : c)
			success = remove(o) && success;
		return success;
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		expirationCheck();
		return internalMap.keySet().retainAll(c);
	}

	@Override
	public int size()
	{
		expirationCheck();
		return internalMap.size();
	}

	@Override
	public Object[] toArray()
	{
		expirationCheck();
		return internalMap.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		expirationCheck();
		return internalMap.keySet().toArray(arg0);
	}
}
