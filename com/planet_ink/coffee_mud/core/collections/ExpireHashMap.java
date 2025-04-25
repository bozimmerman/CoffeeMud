package com.planet_ink.coffee_mud.core.collections;

import java.util.*;
import java.util.Map.Entry;

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
 * A Map that does not return items after an elapsed period of time after being added.
 */
public class ExpireHashMap<K,J> implements Map<K, J>
{
	private final Map<K,Pair<J,Long>> internalMap;
	private long expirationTime = 2 * 60000; // two minutes

	protected Long defTime()
	{
		return Long.valueOf(System.currentTimeMillis() + expirationTime);
	}


	public ExpireHashMap()
	{
		internalMap = new SHashtable<K,Pair<J,Long>>();
	}

	public ExpireHashMap(final long expirationMs)
	{
		this();
		this.expirationTime = expirationMs;
	}

	public ExpireHashMap(final Map<K,J> s)
	{
		internalMap = new SHashtable<K,Pair<J,Long>>();
		putAll(s);
	}

	@Override
	public void clear()
	{
		internalMap.clear();
	}

	@Override
	public boolean containsKey(final Object arg0)
	{
		final Pair<J,Long> l = internalMap.get(arg0);
		if(l == null)
			return false;
		if(System.currentTimeMillis()<l.second.longValue())
			return true;
		internalMap.remove(arg0);
		return false;
	}

	protected Filterer<K> fconv = new Filterer<K>()
	{
		@Override
		public boolean passesFilter(final K obj)
		{
			if(obj == null)
				return false;
			final Pair<J,Long> L = internalMap.get(obj);
			if(L == null)
				return false;
			if(System.currentTimeMillis()<L.second.longValue())
				return true;
			internalMap.remove(obj);
			return false;
		}
	};

	protected Converter<Entry<K,Pair<J,Long>>, Entry<K,J>> entryConverter =
			new Converter<Entry<K,Pair<J,Long>>, Entry<K,J>>() {
				@Override
				public Entry<K, J> convert(final Entry<K, Pair<J, Long>> obj)
				{
					return new Entry<K, J>() {
						final K key = obj.getKey();
						final J val = obj.getValue().first;
						@Override
						public K getKey()
						{
							return key;
						}

						@Override
						public J getValue()
						{
							return val;
						}

						@Override
						public J setValue(final J value)
						{
							final J oldVal = get(key);
							put(key, value);
							return oldVal;
						}
					};
				}
	};

	protected Converter<Pair<J,Long>, J> pairConverter =
			new Converter<Pair<J,Long>, J>() {
				@Override
				public J convert(final Pair<J, Long> obj)
				{
					return obj.first;
				}
	};

	@Override
	public J remove(final Object o)
	{
		final Pair<J,Long> l = internalMap.remove(o);
		if(l == null)
			return null;
		if(System.currentTimeMillis()<l.second.longValue())
			return l.first;
		return null;
	}

	@Override
	public int size()
	{
		return internalMap.size();
	}


	@Override
	public boolean isEmpty()
	{
		return internalMap.isEmpty();
	}


	@Override
	public boolean containsValue(final Object value)
	{
		for(final K key : keySet())
			if(get(key)==value)
				return true;
		return false;
	}


	@Override
	public J get(final Object key)
	{
		final Pair<J,Long> l = internalMap.get(key);
		if(l == null)
			return null;
		if(System.currentTimeMillis()<l.second.longValue())
			return l.first;
		internalMap.remove(key);
		return null;
	}


	@Override
	public J put(final K key, final J value)
	{
		final J oldJ = get(key);
		internalMap.put(key, new Pair<J,Long>(value,defTime()));
		return oldJ;
	}


	@Override
	public void putAll(final Map<? extends K, ? extends J> m)
	{
		for(final K key : m.keySet())
			put(key,m.get(key));
	}


	@Override
	public Set<K> keySet()
	{
		return new FilteredSetWrapper<K>(internalMap.keySet(), fconv);
	}


	@Override
	public Collection<J> values()
	{
		return new ConvertingCollection<Pair<J,Long>,J>(internalMap.values(), pairConverter);
	}


	@Override
	public Set<Entry<K, J>> entrySet()
	{
		return new ConvertingSet<Entry<K,Pair<J,Long>>, Entry<K, J>>(internalMap.entrySet(), entryConverter);
	}

}
