package com.planet_ink.coffee_mud.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.planet_ink.coffee_mud.core.collections.LinkedCollection.LinkedEntry;
/*
   Copyright 2013-2020 Bo Zimmerman

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
public class OrderedMap<K,J> implements Map<K,J>,  Iterable<J>
{
	private final LinkedCollection<Pair<K, J>>			coll	= new LinkedCollection<Pair<K, J>>();
	private final HashMap<K, LinkedEntry<Pair<K, J>>>	map		= new HashMap<K, LinkedEntry<Pair<K, J>>>();

	@SuppressWarnings("rawtypes" )
	private static final Iterator empty=EmptyIterator.INSTANCE;

	private final Converter<Pair<K,J>,J> converter = new Converter<Pair<K,J>,J>()
	{
		@Override
		public J convert(final Pair<K, J> obj)
		{
			return obj.second;
		}

	};

	private final Converter<Pair<K,J>,Map.Entry<K,J>> entryConverter = new Converter<Pair<K,J>,Map.Entry<K,J>>()
	{
		@Override
		public Map.Entry<K,J> convert(final Pair<K, J> obj)
		{
			return obj;
		}

	};

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<J> iterator()
	{
		if(size()==0)
			return empty;
		return new ConvertingIterator<Pair<K,J>,J>(coll.iterator(), converter);
	}

	@SuppressWarnings("unchecked")
	public Iterator<Pair<K,J>> pairIterator()
	{
		if(size()==0)
			return empty;
		return coll.iterator();
	}

	@Override
	public synchronized J put(final K key, final J value)
	{
		final LinkedEntry<Pair<K,J>> p;
		if(map.containsKey(key))
		{
			p = map.get(key);
			p.value.second = value;
		}
		else
			p=new LinkedEntry<Pair<K,J>>(new Pair<K,J>(key, value));
		coll.add(p);
		final LinkedEntry<Pair<K,J>> added = map.put(key, p);
		if(added != null)
			return added.value.second;
		return null;
	}

	@Override
	public synchronized void putAll(final Map<? extends K, ? extends J> t)
	{
		for(final Map.Entry<? extends K,? extends J> i : t.entrySet())
			put(i.getKey(),i.getValue());
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public synchronized J remove(final Object key)
	{
		if(map.containsKey(key))
		{
			final LinkedEntry<Pair<K,J>> l = map.get(key);
			if(coll.remove(l))
				return l.value.second;

		}
		return null;
	}

	@Override
	public synchronized void clear()
	{
		map.clear();
		coll.clear();
	}

	@Override
	public synchronized int size()
	{
		return map.size();
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public synchronized boolean containsKey(final Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public synchronized boolean containsValue(final Object value)
	{
		return coll.contains(value);
	}

	@Override
	public synchronized J get(final Object key)
	{
		if(map.containsKey(key))
			return map.get(key).value.second;
		return null;
	}

	@Override
	public synchronized Set<K> keySet()
	{
		return map.keySet();
	}

	@Override
	public synchronized Collection<J> values()
	{
		return new ConvertingCollection<Pair<K,J>, J>(coll,converter);
	}

	@Override
	public synchronized Set<Entry<K, J>> entrySet()
	{
		return new ConvertingSet<Pair<K,J>, Map.Entry<K,J>>(coll,entryConverter);
	}
}
