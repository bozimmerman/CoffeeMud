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

import com.planet_ink.coffee_mud.core.collections.LinkedSet.LinkedEntry;
/*
   Copyright 2013-2025 Bo Zimmerman

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
/**
 * A simple linked list implementation of a Map, that also maintains the order
 * in which entries were added.
 *
 * @param <K> the type of key
 * @param <J> the type of value
 * @author Bo Zimmerman
 */
public class OrderedMap<K,J> implements Map<K,J>,  Iterable<J>
{
	private final LinkedSet<Pair<K, J>>					coll	= new LinkedSet<Pair<K, J>>();
	private final HashMap<K, LinkedEntry<Pair<K, J>>>	map		= new HashMap<K, LinkedEntry<Pair<K, J>>>();

	@SuppressWarnings("rawtypes" )
	private static final Iterator empty=EmptyIterator.INSTANCE;

	/**
	 * A converter that converts a Pair to its second value.
	 */
	private final Converter<Pair<K,J>,J> converter = new Converter<Pair<K,J>,J>()
	{
		@Override
		public J convert(final Pair<K, J> obj)
		{
			return obj.second;
		}

	};

	/**
	 * A converter that converts a Pair to its first value.
	 */
	private final Converter<Pair<K,J>,K> keyConverter = new Converter<Pair<K,J>,K>()
	{
		@Override
		public K convert(final Pair<K, J> obj)
		{
			return obj.first;
		}

	};

	/**
	 * A converter that converts a Pair to a Map.Entry
	 */
	private final Converter<Pair<K,J>,Map.Entry<K,J>> entryConverter = new Converter<Pair<K,J>,Map.Entry<K,J>>()
	{
		@Override
		public Map.Entry<K,J> convert(final Pair<K, J> obj)
		{
			return obj;
		}

	};

	/**
	 * An iterator for the values in the map.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<J> iterator()
	{
		if(size()==0)
			return empty;
		return new ConvertingIterator<Pair<K,J>,J>(coll.iterator(), converter);
	}

	/**
	 * An iterator for the keys in the map.
	 * @return an iterator of the keys
	 */
	@SuppressWarnings("unchecked")
	public Iterator<K> keyIterator()
	{
		if(size()==0)
			return empty;
		return new ConvertingIterator<Pair<K,J>,K>(coll.iterator(), keyConverter);
	}

	/**
	 * An iterator for the key-value pairs in the map.
	 * @return an iterator of the key-value pairs
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Pair<K,J>> pairIterator()
	{
		if(size()==0)
			return empty;
		return coll.iterator();
	}

	/**
	 * Adds the given key-value pair to the map, replacing any previous value
	 * associated with the key. If the key is already in the map, its value
	 * is replaced, but its position in the order is not changed. If the key is
	 * not already in the map, it is added to the end of the order.
	 *
	 * @param key the key
	 * @param value the value
	 */
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

	/**
	 * Adds all the given key-value pairs to the map, replacing any previous
	 * values associated with the keys. If any of the keys are not already in
	 * the map, they are added to the end of the order.
	 *
	 * @param t the map of key-value pairs to add
	 */
	@Override
	public synchronized void putAll(final Map<? extends K, ? extends J> t)
	{
		for(final Map.Entry<? extends K,? extends J> i : t.entrySet())
			put(i.getKey(),i.getValue());
	}

	/**
	 * Removes the given key and its associated value from the map.
	 *
	 * @param key the key to remove
	 * @return the value that was associated with the key, or null if none
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public synchronized J remove(final Object key)
	{
		if(map.containsKey(key))
		{
			final LinkedEntry<Pair<K,J>> l = map.get(key);
			if(coll.remove(l))
			{
				map.remove(key);
				return l.value.second;
			}

		}
		return null;
	}

	/**
	 * Removes all keys and values from the map.
	 */
	@Override
	public synchronized void clear()
	{
		map.clear();
		coll.clear();
	}

	/**
	 * Returns the number of key-value pairs in the map.
	 *
	 * @return the number of key-value pairs in the map
	 */
	@Override
	public synchronized int size()
	{
		return map.size();
	}

	/**
	 * Returns true if the map contains no key-value pairs.
	 *
	 * @return true if the map contains no key-value pairs
	 */
	@Override
	public synchronized boolean isEmpty()
	{
		return map.isEmpty();
	}

	/**
	 * Returns true if the map contains the given key.
	 *
	 * @param key the key to check for
	 * @return true if the map contains the given key
	 */
	@Override
	public synchronized boolean containsKey(final Object key)
	{
		return map.containsKey(key);
	}

	/**
	 * Returns true if the map contains the given value.
	 *
	 * @param value the value to check for
	 * @return true if the map contains the given value
	 */
	@Override
	public synchronized boolean containsValue(final Object value)
	{
		return coll.contains(value);
	}

	/**
	 * Returns the value associated with the given key, or null if none.
	 *
	 * @param key the key to look up
	 * @return the value associated with the given key, or null if none
	 */
	@Override
	public synchronized J get(final Object key)
	{
		if(map.containsKey(key))
			return map.get(key).value.second;
		return null;
	}

	/**
	 * Returns a Set view of the keys in the map, in the order they were added.
	 *
	 * @return a Set view of the keys in the map
	 */
	@Override
	public synchronized Set<K> keySet()
	{
		return map.keySet();
	}

	/**
	 * Returns a Collection view of the values in the map, in the order they
	 * were added.
	 *
	 * @return a Collection view of the values in the map
	 */
	@Override
	public synchronized Collection<J> values()
	{
		return new ConvertingCollection<Pair<K,J>, J>(coll,converter);
	}

	/**
	 * Returns a Set view of the key-value pairs in the map, in the order they
	 * were added.
	 *
	 * @return a Set view of the key-value pairs in the map
	 */
	@Override
	public synchronized Set<Entry<K, J>> entrySet()
	{
		return new ConvertingSet<Pair<K,J>, Map.Entry<K,J>>(coll,entryConverter);
	}
}
