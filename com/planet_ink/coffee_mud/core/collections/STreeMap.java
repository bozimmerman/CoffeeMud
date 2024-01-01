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
import java.util.concurrent.locks.ReentrantLock;

/*
   Copyright 2010-2024 Bo Zimmerman

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
	private static final long				serialVersionUID	= -6713012858839312626L;
	private volatile TreeMap<K, V>			T;
	private transient final ReentrantLock	lock				= new ReentrantLock();

	public STreeMap()
	{
		T=new TreeMap<K,V>();
	}

	public STreeMap(final Comparator<K> comp)
	{
		T=new TreeMap<K,V>(comp);
	}

	public STreeMap(final Map<K,V> E)
	{
		T=new TreeMap<K,V>();
		if(E!=null)
			for(final K o : E.keySet())
				put(o,E.get(o));
	}

	@SuppressWarnings("unchecked")
	public TreeMap<K,V> toTreeMap()
	{
		return (TreeMap<K,V>)T.clone();
	}

	public Vector<String> toStringVector(final String divider)
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
	public java.util.Map.Entry<K, V> ceilingEntry(final K key)
	{
		return T.ceilingEntry(key);
	}

	@Override
	public K ceilingKey(final K key)
	{
		return T.ceilingKey(key);
	}

	@Override
	public void clear()
	{
		if(T.size()==0)
			return;
		final ReentrantLock lock = this.lock;
		lock.lock();
		try
		{
			this.T = new TreeMap<K, V>();
		}
		finally
		{
			lock.unlock();
		}
	}

	public STreeMap<K,V> copyOf()
	{
		final STreeMap<K,V> SH=new STreeMap<K,V>();
		SH.T=new TreeMap<K,V>(T);
		return SH;
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return T.comparator();
	}

	@Override
	public boolean containsKey(final Object key)
	{
		return T.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value)
	{
		return T.containsValue(value);
	}

	@Override
	public NavigableSet<K> descendingKeySet()
	{
		return new ReadOnlyNavigableSet<K>(T.descendingKeySet());
	}

	@Override
	public NavigableMap<K, V> descendingMap()
	{
		return new ReadOnlyNavigableMap<K,V>(T.descendingMap());
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return new ReadOnlySet<java.util.Map.Entry<K, V>>(T.entrySet());
	}

	@Override
	public java.util.Map.Entry<K, V> firstEntry()
	{
		return T.firstEntry();
	}

	@Override
	public K firstKey()
	{
		return T.firstKey();
	}

	@Override
	public java.util.Map.Entry<K, V> floorEntry(final K key)
	{
		return T.floorEntry(key);
	}

	@Override
	public K floorKey(final K key)
	{
		return T.floorKey(key);
	}

	@Override
	public V get(final Object key)
	{
		return T.get(key);
	}

	@Override
	public NavigableMap<K, V> headMap(final K toKey, final boolean inclusive)
	{
		return new ReadOnlyNavigableMap<K,V>(T.headMap(toKey, inclusive));
	}

	@Override
	public SortedMap<K, V> headMap(final K toKey)
	{
		return new ReadOnlySortedMap<K,V>(T.headMap(toKey));
	}

	@Override
	public java.util.Map.Entry<K, V> higherEntry(final K key)
	{
		return T.higherEntry(key);
	}

	@Override
	public K higherKey(final K key)
	{
		return T.higherKey(key);
	}

	@Override
	public Set<K> keySet()
	{
		return new ReadOnlySet<K>(T.keySet());
	}

	@Override
	public java.util.Map.Entry<K, V> lastEntry()
	{
		return T.lastEntry();
	}

	@Override
	public K lastKey()
	{
		return T.lastKey();
	}

	@Override
	public java.util.Map.Entry<K, V> lowerEntry(final K key)
	{
		return T.lowerEntry(key);
	}

	@Override
	public K lowerKey(final K key)
	{
		return T.lowerKey(key);
	}

	@Override
	public NavigableSet<K> navigableKeySet()
	{
		return new ReadOnlyNavigableSet<K>(T.navigableKeySet());
	}

	@Override
	public java.util.Map.Entry<K, V> pollFirstEntry()
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeMap<K, V> T2 = new TreeMap<K, V>(T);
		try
		{
			return T2.pollFirstEntry();
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public java.util.Map.Entry<K, V> pollLastEntry()
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeMap<K, V> T2 = new TreeMap<K, V>(T);
		try
		{
			return T2.pollLastEntry();
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public V put(final K key, final V value)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeMap<K, V> T2 = new TreeMap<K, V>(T);
		try
		{
			return T2.put(key, value);
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> map)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeMap<K, V> T2 = new TreeMap<K, V>(T);
		try
		{
			T2.putAll(map);
		}
		finally
		{
			this.T=T2;
			lock.unlock();
		}
	}

	@Override
	public V remove(final Object key)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final TreeMap<K, V> T2 = new TreeMap<K, V>(T);
		try
		{
			return T2.remove(key);
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
	public NavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive)
	{
		return new ReadOnlyNavigableMap<K,V>(T.subMap(fromKey, fromInclusive, toKey, toInclusive));
	}

	@Override
	public SortedMap<K, V> subMap(final K fromKey, final K toKey)
	{
		return new ReadOnlySortedMap<K,V>(T.subMap(fromKey, toKey));
	}

	@Override
	public NavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive)
	{
		return new ReadOnlyNavigableMap<K, V>(T.tailMap(fromKey, inclusive));
	}

	@Override
	public SortedMap<K, V> tailMap(final K fromKey)
	{
		return new ReadOnlySortedMap<K, V>(T.tailMap(fromKey));
	}

	@Override
	public Collection<V> values()
	{
		return new ReadOnlyCollection<V>(T.values());
	}

	@Override
	public boolean equals(final Object o)
	{
		return this==o;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean isEmpty()
	{
		return T.isEmpty();
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

}
