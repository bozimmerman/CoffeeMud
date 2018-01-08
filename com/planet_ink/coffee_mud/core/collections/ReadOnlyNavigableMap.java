package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;

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
public class ReadOnlyNavigableMap<K, V> implements NavigableMap<K, V>
{
	private final NavigableMap<K, V>	map;

	public ReadOnlyNavigableMap(NavigableMap<K, V> s)
	{
		map = s;
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

	@Override
	public java.util.Map.Entry<K, V> ceilingEntry(K key)
	{
		return map.ceilingEntry(key);
	}

	@Override
	public K ceilingKey(K key)
	{
		return map.ceilingKey(key);
	}

	@Override
	public NavigableSet<K> descendingKeySet()
	{
		return new ReadOnlyNavigableSet<K>(map.descendingKeySet());
	}

	@Override
	public NavigableMap<K, V> descendingMap()
	{
		return new ReadOnlyNavigableMap<K, V>(map.descendingMap());
	}

	@Override
	public java.util.Map.Entry<K, V> firstEntry()
	{
		return map.firstEntry();
	}

	@Override
	public java.util.Map.Entry<K, V> floorEntry(K key)
	{
		return map.floorEntry(key);
	}

	@Override
	public K floorKey(K key)
	{
		return map.floorKey(key);
	}

	@Override
	public NavigableMap<K, V> headMap(K toKey, boolean inclusive)
	{
		return new ReadOnlyNavigableMap<K, V>(map.headMap(toKey, inclusive));
	}

	@Override
	public java.util.Map.Entry<K, V> higherEntry(K key)
	{
		return map.higherEntry(key);
	}

	@Override
	public K higherKey(K key)
	{
		return map.higherKey(key);
	}

	@Override
	public java.util.Map.Entry<K, V> lastEntry()
	{
		return map.lastEntry();
	}

	@Override
	public java.util.Map.Entry<K, V> lowerEntry(K key)
	{
		return map.lowerEntry(key);
	}

	@Override
	public K lowerKey(K key)
	{
		return map.lowerKey(key);
	}

	@Override
	public NavigableSet<K> navigableKeySet()
	{
		return new ReadOnlyNavigableSet<K>(map.navigableKeySet());
	}

	@Override
	public java.util.Map.Entry<K, V> pollFirstEntry()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public java.util.Map.Entry<K, V> pollLastEntry()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
	{
		return new ReadOnlyNavigableMap<K, V>(map.subMap(fromKey, fromInclusive, toKey, toInclusive));
	}

	@Override
	public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
	{
		return new ReadOnlyNavigableMap<K, V>(map.tailMap(fromKey, inclusive));
	}

}
