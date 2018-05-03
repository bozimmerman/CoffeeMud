package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
public class ReadOnlySortedMap<K, V> implements SortedMap<K, V>
{
	private final SortedMap<K, V>	map;

	public ReadOnlySortedMap(SortedMap<K, V> s)
	{
		map = s;
	}

	public ReadOnlySortedMap()
	{
		map = new TreeMap<K,V>();
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return map.comparator();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return new ReadOnlySet<java.util.Map.Entry<K, V>>(map.entrySet());
	}

	@Override
	public K firstKey()
	{
		return map.firstKey();
	}

	@Override
	public SortedMap<K, V> headMap(K toKey)
	{
		return new ReadOnlySortedMap<K, V>(map.headMap(toKey));
	}

	@Override
	public Set<K> keySet()
	{
		return new ReadOnlySet<K>(map.keySet());
	}

	@Override
	public K lastKey()
	{
		return map.lastKey();
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey)
	{
		return new ReadOnlySortedMap<K, V>(map.subMap(fromKey, toKey));
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey)
	{
		return new ReadOnlySortedMap<K, V>(map.tailMap(fromKey));
	}

	@Override
	public Collection<V> values()
	{
		return new ReadOnlyCollection<V>(map.values());
	}

	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return map.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public V put(K key, V value)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public V remove(Object key)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return map.size();
	}

}
