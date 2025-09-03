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
 * A TreeSet that is limited in size, and also limited in how long an entry can
 * remain in the set without being accessed. When the max size is exceeded, or
 * when an entry ages out, it is removed from the set. Accessing an entry (via
 * add() or contains()) updates its last-accessed time.
 *
 * @param <K> the type of value
 * @author Bo Zimmerman
 */
public class LimitedTreeSet<K> extends TreeSet<K>
{
	private static final long serialVersionUID = 5949532522375107316L;

	private final long		expireMs;
	private int				max;
	private long			nextCheck	= 0;
	private final boolean	caseLess;
	private final boolean	grow;

	/** The map of keys to their last-accessed time */
	private final OrderedMap<K,long[]> expirations;

	/**
	 * Constructor
	 *
	 * @param expireMs the number of milliseconds an entry can remain idle in
	 *            the set before expiring
	 * @param max the maximum number of entries allowed in the set
	 * @param caseInsensitive true to make String keys case insensitive
	 * @param grow true to allow the max to grow if necessary, false to enforce
	 *            it strictly
	 */
	public LimitedTreeSet(final long expireMs, final int max, final boolean caseInsensitive, final boolean grow)
	{
		super(new Comparator<Object>()
		{
			@Override
			public int compare(final Object o1, final Object o2)
			{
				if(o1 == null)
				{
					if(o2 == null)
						return 0;
					return -1;
				}
				else
				if(o2 == null)
					return 1;
				if((o1 instanceof String)
				&&(o2 instanceof String))
					return caseInsensitive?((String)o1).compareToIgnoreCase((String)o2):((String)o1).compareTo((String)o2);
				final int hc1 = o1.hashCode();
				final int hc2 = o2.hashCode();
				return (hc1==hc2)?0:(hc1>hc2)?1:-1;
			}

		});
		this.caseLess=caseInsensitive;
		expirations=new OrderedMap<K,long[]>();
		this.expireMs=expireMs;
		this.max=max;
		this.grow=grow;
	}

	/**
	 * Constructor - default grow=false
	 *
	 * @param expireMs the number of milliseconds an entry can remain idle in
	 *            the set before expiring
	 * @param max the maximum number of entries allowed in the set
	 * @param caseInsensitive true to make String keys case insensitive, false
	 *            otherwise
	 */
	public LimitedTreeSet(final long expireMs, final int max, final boolean caseInsensitive)
	{
		this(expireMs, max, caseInsensitive, false);
	}

	/**
	 * Constructor - default 1 minute expiration, 100 max entries, case
	 * Sensitive keys, grow=false
	 */
	public LimitedTreeSet()
	{
		this(60000,100,false);
	}

	/**
	 * Adds the given key to the set.
	 *
	 * @param key the key to add
	 * @return true if not already present, false otherwise
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean add(K key)
	{
		if(key instanceof String)
			key = (K)(caseLess?((String)key).toLowerCase():key);
		check();
		synchronized(expirations)
		{
			final boolean k = super.add(key);
			expirations.put(key, new long[] {System.currentTimeMillis()});
			return k;
		}
	}

	/**
	 * Adds all the keys in the given collection to the set.
	 *
	 * @param map the collection of keys to add
	 * @return true if at least one was added, false otherwise
	 */
	@Override
	public boolean addAll(final Collection<? extends K> map)
	{
		// this long put ensures the case insensitivity
		boolean ok=true;
		for(final K e : map)
			ok=this.add(e) && ok;
		return ok;
	}

	/**
	 * Checks for and removes any expired entries, and if the size is over the
	 * max, removes the oldest accessed entries until it is not.
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
					for(final Iterator<Pair<K,long[]>> v = expirations.pairIterator();v.hasNext();)
					{
						final Pair<K,long[]> p=v.next();
						if(p.second[0] >= then)
							break;
						v.remove();
						this.internalRemove(p.first);
					}
				}
				if(grow && (size()>max))
				{
					max=size();
					break;
				}
				then += expireMs/10;
			}
			while(size()>max);
		}
	}

	/**
	 * Checks to see if the given key is in the set. If the key is a String, and
	 * this set was constructed with case insensitivity, the key will be
	 * converted to lower-case before checking. Accessing an entry (via add() or
	 * contains()) updates its last-accessed time.
	 *
	 * @param key the key to check for
	 * @return true if the key is in the set, false otherwise
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object key)
	{
		if(key instanceof String)
			key = caseLess?(K)((String)key).toLowerCase():key;
		check();
		synchronized(expirations)
		{
			final boolean c=super.contains(key);
			if(c)
				expirations.put((K)key, new long[] {System.currentTimeMillis()});
			return c;
		}
	}

	/**
	 * Returns the number of keys in the set.
	 *
	 * @return the number of keys
	 */
	@Override
	public Iterator<K> iterator()
	{
		return super.iterator();
	}

	/**
	 * Empties the set.
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
	 * Removes the given key from the set. If the key is a String, and this set
	 * was constructed with case insensitivity, the key will be converted to
	 * lower-case before checking.
	 *
	 * @param key the key to remove
	 * @return true if the key was found and removed, false otherwise
	 */
	protected boolean internalRemove(Object key)
	{
		if(key instanceof String)
			key = caseLess?((String)key).toLowerCase():key;
		synchronized(expirations)
		{
			final boolean obj=super.remove(key);
			expirations.remove(key);
			return obj;
		}
	}

	/**
	 * Removes the given key from the set. If the key is a String, and this set
	 * was constructed with case insensitivity, the key will be converted to
	 * lower-case before checking.
	 *
	 * @param key the key to remove
	 * @return true if the key was found and removed, false otherwise
	 */
	@Override
	public boolean remove(final Object key)
	{
		check();
		return internalRemove(key);
	}
}
