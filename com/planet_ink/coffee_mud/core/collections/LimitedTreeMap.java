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
public class LimitedTreeMap<K> extends TreeMap<String,K>
{
	private static final long serialVersionUID = 5949532522375107316L;

	private final long expireMs;
	private final int max;
	private long nextCheck = 0;
	private final boolean caseLess;
	private final OrderedMap<String,long[]> expirations;

	public LimitedTreeMap(final long expireMs, final int max, final boolean caseInsensitive)
	{
		super(caseInsensitive?new Comparator<String>()
		{
			@Override
			public int compare(final String arg0, final String arg1)
			{
				return arg0.compareToIgnoreCase(arg1);
			}
		}:new Comparator<String>()
		{
			@Override
			public int compare(final String arg0, final String arg1)
			{
				return arg0.compareTo(arg1);
			}
		});
		this.caseLess=caseInsensitive;
		expirations=new OrderedMap<String,long[]>();
		this.expireMs=expireMs;
		this.max=max;
	}

	public LimitedTreeMap()
	{
		this(60000,100,false);
	}

	@Override
    public K put(String key, final K value)
	{
		key = caseLess?key.toLowerCase():key;
		check();
    	final K k = super.put(key, value);
		synchronized(expirations)
		{
			expirations.put(key, new long[] {System.currentTimeMillis()});
		}
    	return k;
    }

	@Override
    public void putAll(final Map<? extends String, ? extends K> map)
	{
		// this long put ensures the case insensitivity
		for(final Map.Entry<? extends String, ? extends K> e : map.entrySet())
			this.put(e.getKey(),e.getValue());
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
					for(final Iterator<Pair<String,long[]>> v = expirations.pairIterator();v.hasNext();)
					{
						final Pair<String,long[]> p=v.next();
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

	@Override
    public boolean containsKey(Object key)
    {
		if(key instanceof String)
			key = caseLess?((String)key).toLowerCase():key;
		check();
    	final boolean c=super.containsKey(key);
    	if((c)&&(key instanceof String))
    	{
    		synchronized(expirations)
    		{
	    		expirations.put((String)key, new long[] {System.currentTimeMillis()});
    		}
    	}
    	return c;
    }

	@Override
    public K get(Object key)
    {
		if(key instanceof String)
			key = caseLess?((String)key).toLowerCase():key;
		check();
    	final K obj=super.get(key);
    	if(super.containsKey(key)&&(key instanceof String))
    	{
    		synchronized(expirations)
    		{
	    		expirations.put((String)key, new long[] {System.currentTimeMillis()});
    		}
    	}
    	return obj;
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

	protected K internalRemove(Object key)
	{
		if(key instanceof String)
			key = caseLess?((String)key).toLowerCase():key;
    	final K obj=super.remove(key);
		synchronized(expirations)
		{
    		expirations.remove(key);
		}
    	return obj;
	}

	@Override
    public K remove(final Object key)
	{
		check();
		return internalRemove(key);
	}
}
