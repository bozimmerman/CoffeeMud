package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
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
import java.util.WeakHashMap;
/*
   Copyright 2000-2015 Bo Zimmerman

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
public class STreeMap<K,V> implements Serializable, Map<K,V>, NavigableMap<K,V>, SortedMap<K,V>, SafeCollectionHost
{
	private static final long serialVersionUID = -6713012858839312626L;
	private volatile TreeMap<K,V> T;
	private final Date lastIteratorCall = new Date(0);
	
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
			if(S!=null)
			{
				final Object O = get(S);
				if(O==null)
					V.add(S.toString() + divider);
				else
					V.add(S.toString() + divider + O.toString());
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
		if (doClone())
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
		return new SafeChildNavigableSet<K>(T.descendingKeySet(), this);
	}

	@Override
	public synchronized NavigableMap<K, V> descendingMap()
	{
		return new SafeChildNavigableMap<K,V>(T.descendingMap(), this);
	}

	@Override
	public synchronized Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return new SafeChildSet<java.util.Map.Entry<K, V>>(T.entrySet(), this);
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
		return new SafeChildNavigableMap<K,V>(T.headMap(toKey, inclusive), this);
	}

	@Override
	public synchronized SortedMap<K, V> headMap(K toKey)
	{
		return new SafeChildSortedMap<K,V>(T.headMap(toKey), this);
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
		return new SafeChildSet<K>(T.keySet(), this);
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
		return new SafeChildNavigableSet<K>(T.navigableKeySet(), this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized java.util.Map.Entry<K, V> pollFirstEntry()
	{
		if (doClone())
			T=(TreeMap<K,V>)T.clone();
		return T.pollFirstEntry();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized java.util.Map.Entry<K, V> pollLastEntry()
	{
		if (doClone())
			T=(TreeMap<K,V>)T.clone();
		return T.pollLastEntry();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized V put(K key, V value)
	{
		if (doClone())
			T=(TreeMap<K,V>)T.clone();
		return T.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> map)
	{
		if (doClone())
			T=(TreeMap<K,V>)T.clone();
		T.putAll(map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized V remove(Object key)
	{
		if (doClone())
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
		return new SafeChildNavigableMap<K,V>(T.subMap(fromKey, fromInclusive, toKey, toInclusive), this);
	}

	@Override
	public synchronized SortedMap<K, V> subMap(K fromKey, K toKey)
	{
		return new SafeChildSortedMap<K,V>(T.subMap(fromKey, toKey), this);
	}

	@Override
	public synchronized NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
	{
		return new SafeChildNavigableMap<K, V>(T.tailMap(fromKey, inclusive), this);
	}

	@Override
	public synchronized SortedMap<K, V> tailMap(K fromKey)
	{
		return new SafeChildSortedMap<K, V>(T.tailMap(fromKey), this);
	}

	@Override
	public synchronized Collection<V> values()
	{
		return new SafeChildCollection<V>(T.values(), this);
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

	private boolean doClone()
	{
		synchronized(this.lastIteratorCall)
		{
			return System.currentTimeMillis() < this.lastIteratorCall.getTime();
		}
	}
	
	@Override
	public void returnIterator(Object iter) 
	{
	}

	@Override
	public void submitIterator(Object iter) 
	{
		synchronized(this.lastIteratorCall)
		{
			this.lastIteratorCall.setTime(System.currentTimeMillis() + ITERATOR_TIMEOUT_MS);
		}
	}
}
