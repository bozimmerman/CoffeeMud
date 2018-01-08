package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
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
public class STreeMap<K,V> implements Serializable, Map<K,V>, NavigableMap<K,V>, SortedMap<K,V>
{
	private static final long serialVersionUID = -6713012858839312626L;
	private volatile TreeMap<K,V> T;
	public STreeMap()
	{
		T=new TreeMap<K,V>();
	}

	public STreeMap(Comparator<K> comp)
	{
		T=new TreeMap<K,V>(comp);
	}

	public STreeMap(Map<K,V> E)
	{
		T=new TreeMap<K,V>();
		if(E!=null)
			for(final K o : E.keySet())
				put(o,E.get(o));
	}

	@SuppressWarnings("unchecked")
	public synchronized TreeMap<K,V> toTreeMap()
	{
		return (TreeMap<K,V>)T.clone();
	}

	public synchronized Vector<String> toStringVector(String divider)
	{
		final Vector<String> V=new Vector<String>(size());
		for(final Object S : navigableKeySet())
		{
			if(S!=null)
			{
				final Object O = get(S);
				if(O==null)
					V.add(S.toString() + divider);
				else
					V.add(S.toString() + divider + O.toString());
			}
		}
		return V;
	}

	@Override
	public synchronized java.util.Map.Entry<K, V> ceilingEntry(K key)
	{
		return T.ceilingEntry(key);
	}

	@Override
	public synchronized K ceilingKey(K key)
	{
		return T.ceilingKey(key);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void clear()
	{
		T=(TreeMap<K,V>)T.clone();
		T.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized STreeMap<K,V> copyOf()
	{
		final STreeMap<K,V> SH=new STreeMap<K,V>();
		SH.T=(TreeMap<K,V>)T.clone();
		return SH;
	}

	@Override
	public synchronized Comparator<? super K> comparator()
	{
		return T.comparator();
	}

	@Override
	public synchronized boolean containsKey(Object key)
	{
		return T.containsKey(key);
	}

	@Override
	public synchronized boolean containsValue(Object value)
	{
		return T.containsValue(value);
	}

	@Override
	public synchronized NavigableSet<K> descendingKeySet()
	{
		return new ReadOnlyNavigableSet<K>(T.descendingKeySet());
	}

	@Override
	public synchronized NavigableMap<K, V> descendingMap()
	{
		return new ReadOnlyNavigableMap<K,V>(T.descendingMap());
	}

	@Override
	public synchronized Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return new ReadOnlySet<java.util.Map.Entry<K, V>>(T.entrySet());
	}

	@Override
	public synchronized java.util.Map.Entry<K, V> firstEntry()
	{
		return T.firstEntry();
	}

	@Override
	public synchronized K firstKey()
	{
		return T.firstKey();
	}

	@Override
	public synchronized java.util.Map.Entry<K, V> floorEntry(K key)
	{
		return T.floorEntry(key);
	}

	@Override
	public synchronized K floorKey(K key)
	{
		return T.floorKey(key);
	}

	@Override
	public synchronized V get(Object key)
	{
		return T.get(key);
	}

	@Override
	public synchronized NavigableMap<K, V> headMap(K toKey, boolean inclusive)
	{
		return new ReadOnlyNavigableMap<K,V>(T.headMap(toKey, inclusive));
	}

	@Override
	public synchronized SortedMap<K, V> headMap(K toKey)
	{
		return new ReadOnlySortedMap<K,V>(T.headMap(toKey));
	}

	@Override
	public synchronized java.util.Map.Entry<K, V> higherEntry(K key)
	{
		return T.higherEntry(key);
	}

	@Override
	public synchronized K higherKey(K key)
	{
		return T.higherKey(key);
	}

	@Override
	public synchronized Set<K> keySet()
	{
		return new ReadOnlySet<K>(T.keySet());
	}

	@Override
	public synchronized java.util.Map.Entry<K, V> lastEntry()
	{
		return T.lastEntry();
	}

	@Override
	public synchronized K lastKey()
	{
		return T.lastKey();
	}

	@Override
	public synchronized java.util.Map.Entry<K, V> lowerEntry(K key)
	{
		return T.lowerEntry(key);
	}

	@Override
	public synchronized K lowerKey(K key)
	{
		return T.lowerKey(key);
	}

	@Override
	public synchronized NavigableSet<K> navigableKeySet()
	{
		return new ReadOnlyNavigableSet<K>(T.navigableKeySet());
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized java.util.Map.Entry<K, V> pollFirstEntry()
	{
		T=(TreeMap<K,V>)T.clone();
		return T.pollFirstEntry();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized java.util.Map.Entry<K, V> pollLastEntry()
	{
		T=(TreeMap<K,V>)T.clone();
		return T.pollLastEntry();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized V put(K key, V value)
	{
		T=(TreeMap<K,V>)T.clone();
		return T.put(key, value);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> map)
	{
		T=(TreeMap<K,V>)T.clone();
		T.putAll(map);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized V remove(Object key)
	{
		T=(TreeMap<K,V>)T.clone();
		return T.remove(key);
	}

	@Override
	public synchronized int size()
	{
		return T.size();
	}

	@Override
	public synchronized NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey,
			boolean toInclusive)
			{
		return new ReadOnlyNavigableMap<K,V>(T.subMap(fromKey, fromInclusive, toKey, toInclusive));
	}

	@Override
	public synchronized SortedMap<K, V> subMap(K fromKey, K toKey)
	{
		return new ReadOnlySortedMap<K,V>(T.subMap(fromKey, toKey));
	}

	@Override
	public synchronized NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
	{
		return new ReadOnlyNavigableMap<K, V>(T.tailMap(fromKey, inclusive));
	}

	@Override
	public synchronized SortedMap<K, V> tailMap(K fromKey)
	{
		return new ReadOnlySortedMap<K, V>(T.tailMap(fromKey));
	}

	@Override
	public synchronized Collection<V> values()
	{
		return new ReadOnlyCollection<V>(T.values());
	}

	@Override
	public boolean equals(Object o)
	{
		return this==o;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return T.isEmpty();
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

}
