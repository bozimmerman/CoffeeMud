package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
   Copyright 2020-2025 Bo Zimmerman

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
 * A TreeMap that is limited in size, and also limited in how long an entry can
 * remain in the map without being accessed. When the max size is exceeded, or
 * when an entry ages out, it is removed from the map. Accessing an entry (via
 * get() or containsKey()) updates its last-accessed time.
 *
 * @param <L> the type of key
 * @param <K> the type of value
 * @author Bo Zimmerman
 */
public class LimitedTreeMap<L,K> extends TreeMap<L,K>
{
	private static final long serialVersionUID = 5949532522375107316L;

	private final long					expireMs;
	private final int					max;
	private long						nextCheck	= 0;
	private final boolean				caseLess;
	private final OrderedMap<L, long[]>	expirations;

	/**
	 * Constructor
	 *
	 * @param expireMs the number of milliseconds an entry can remain idle in
	 *            the map before expiring
	 * @param max the maximum number of entries allowed in the map
	 * @param caseInsensitive true to make String keys case insensitive, false
	 *            otherwise
	 */
	public LimitedTreeMap(final long expireMs, final int max, final boolean caseInsensitive)
	{
		super(new Comparator<L>()
		{
			private final boolean caseLess=caseInsensitive;
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(final L arg0, final L arg1)
			{
				if((arg0 instanceof String)&&(arg1 instanceof String)&&(caseLess))
					return ((String)arg0).compareToIgnoreCase((String)arg1);
				if(arg0 instanceof Comparable)
					return ((Comparable)arg0).compareTo(arg1);
				if(arg0==null)
					return (arg1==null)?0:-1;
				if(arg1==null)
					return 1;
				return (arg0.hashCode()==arg1.hashCode())?0:(arg0.hashCode()<arg1.hashCode())?-1:1;
			}
		});
		this.caseLess=caseInsensitive;
		expirations=new OrderedMap<L,long[]>();
		this.expireMs=expireMs;
		this.max=max;
	}

	/**
	 * Constructor - default 1 minute expiration, 100 max entries, case
	 * Sensitive keys
	 */
	public LimitedTreeMap()
	{
		this(60000,100,false);
	}

	/**
	 * Adds the given key and value to the map, replacing any previous value
	 * associated with the key. If the key is a String, and this map was
	 * constructed with case insensitivity, the key will be converted to
	 * lower-case before being added. If the addition of this new key will
	 * exceed the max size of the map, or if any existing keys have aged out,
	 * they will be removed first. Accessing an entry (via get() or
	 * containsKey()) updates its last-accessed time.
	 *
	 * @param key the key
	 * @param value the value
	 */
	@SuppressWarnings("unchecked")
	@Override
	public K put(L key, final K value)
	{
		if(key instanceof String)
			key = caseLess?(L)((String)key).toLowerCase():key;
		check();
		synchronized(expirations)
		{
			final K k = super.put(key, value);
			expirations.put(key, new long[] {System.currentTimeMillis()});
			return k;
		}
	}

	/**
	 * Adds all the given keys and values to the map, replacing any previous
	 * values associated with the keys. If any of the keys are Strings, and this
	 * map was constructed with case insensitivity, those keys will be converted
	 * to lower-case before being added. If the addition of these new keys will
	 * exceed the max size of the map, or if any existing keys have aged out,
	 * they will be removed first. Accessing an entry (via get() or
	 * containsKey()) updates its last-accessed time.
	 *
	 * @param map the map of key-value pairs to add
	 */
	@Override
	public void putAll(final Map<? extends L, ? extends K> map)
	{
		// this long put ensures the case insensitivity
		for(final Map.Entry<? extends L, ? extends K> e : map.entrySet())
			this.put(e.getKey(),e.getValue());
	}

	/**
	 * Check to see if any entries have aged out, and if the size exceeds the
	 * max, and remove those entries if so.
	 */
	protected void check()
	{
		final long now=System.currentTimeMillis();
		if((now > nextCheck)||(size()>max))
		{
			nextCheck=now+expireMs;
			long then=now-expireMs;
			do
			{
				synchronized(expirations)
				{
					for(final Iterator<Pair<L,long[]>> v = expirations.pairIterator();v.hasNext();)
					{
						final Pair<L,long[]> p=v.next();
						if(p.second[0] >= then)
							break;
						v.remove();
						this.internalRemove(p.first);
					}
				}
				then += expireMs/10;
			}
			while(size()>max);
		}
	}

	/**
	 * Checks to see if the given key is in the map. If the key is a String, and
	 * this map was constructed with case insensitivity, the key will be
	 * converted to lower-case before checking. Accessing an entry (via get() or
	 * containsKey()) updates its last-accessed time.
	 *
	 * @param key the key to check for
	 * @return true if the key is in the map, false otherwise
	 */
	@Override
	public boolean containsKey(Object key)
	{
		if(key instanceof String)
			key = caseLess?((String)key).toLowerCase():key;
		check();
		synchronized(expirations)
		{
			@SuppressWarnings("unchecked")
			final L l=(L)key;
			final boolean c=super.containsKey(key);
			if(c)
				expirations.put(l, new long[] {System.currentTimeMillis()});
			return c;
		}
	}

	/**
	 * Gets the value associated with the given key. If the key is a String, and
	 * this map was constructed with case insensitivity, the key will be
	 * converted to lower-case before checking. Accessing an entry (via get() or
	 * containsKey()) updates its last-accessed time.
	 *
	 * @param key the key to get the value for
	 * @return the value associated with the key, or null if not found
	 */
	@Override
	public K get(Object key)
	{
		if(key instanceof String)
			key = caseLess?((String)key).toLowerCase():key;
		check();
		synchronized(expirations)
		{
			final K obj=super.get(key);
			if(super.containsKey(key))
			{
				@SuppressWarnings("unchecked")
				final L l=(L)key;
				expirations.put(l, new long[] {System.currentTimeMillis()});
			}
			return obj;
		}
	}

	/**
	 * Clears all entries from the map.
	 */
	@Override
	public void clear()
	{
		check();
		super.clear();
		synchronized(expirations)
		{
			expirations.clear();
		}
	}

	/**
	 * Removes the entry associated with the given key from the map. If the key
	 * is a String, and this map was constructed with case insensitivity, the
	 * key will be converted to lower-case before checking.
	 *
	 * @param key the key to remove
	 * @return the value that was associated with the key, or null if not found
	 */
	protected K internalRemove(Object key)
	{
		if(key instanceof String)
			key = caseLess?((String)key).toLowerCase():key;
		synchronized(expirations)
		{
			final K obj=super.remove(key);
			expirations.remove(key);
			return obj;
		}
	}

	/**
	 * Removes the entry associated with the given key from the map. If the key
	 * is a String, and this map was constructed with case insensitivity, the
	 * key will be converted to lower-case before checking.
	 *
	 * @param key the key to remove
	 * @return the value that was associated with the key, or null if not found
	 */
	@Override
	public K remove(final Object key)
	{
		check();
		return internalRemove(key);
	}
}
