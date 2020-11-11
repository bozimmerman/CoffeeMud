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
   Copyright 2020-2020 Bo Zimmerman

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
public class LimitedTreeSet<K> extends TreeSet<K>
{
	private static final long serialVersionUID = 5949532522375107316L;

	private final long expireMs;
	private final int max;
	private long nextCheck = 0;
	private final boolean caseLess;
	private final OrderedMap<K,long[]> expirations;

	public LimitedTreeSet(final long expireMs, final int max, final boolean caseInsensitive)
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
	}

	public LimitedTreeSet()
	{
		this(60000,100,false);
	}

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

	@Override
    public boolean addAll(final Collection<? extends K> map)
	{
		// this long put ensures the case insensitivity
		boolean ok=true;
		for(final K e : map)
			ok=this.add(e) && ok;
		return ok;
    }

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
				then += expireMs/10;
			}
			while(size()>max);
		}
	}

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

	@Override
    public boolean remove(final Object key)
	{
		check();
		return internalRemove(key);
	}
}
