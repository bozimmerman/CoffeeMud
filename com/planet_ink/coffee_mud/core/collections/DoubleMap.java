package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
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
 * A Map implementation that keeps a second map of the values to the keys,
 * allowing for reverse lookups.
 *
 * @param <K> the key type
 * @param <F> the value type
 */
public class DoubleMap<K,F> implements java.util.Map<K,F>, java.io.Serializable
{
	private static final long serialVersionUID = 6687178785122561993L;
	private volatile Map<K,F> H1;
	private volatile Map<F,K> H2;

	/**
	 * Construct a new DoubleMap
	 *
	 * @param map1 the map to use for key to value lookups
	 * @param map2 the map to use for value to key lookups
	 */
	public DoubleMap(final Map<K,F> map1, final Map<F,K> map2)
	{
		super();
		H1=map1;
		H2=map2;
	}

	/**
	 * Construct a new DoubleMap
	 *
	 * @param clas the class of map to use for both lookups
	 */
	@SuppressWarnings("unchecked")
	public DoubleMap(final Class<?> clas)
	{
		super();
		try
		{
			H1=(Map<K,F>)clas.newInstance();
			H2=(Map<F,K>)clas.newInstance();
		}
		catch (final Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Returns a vector of strings, each of which is the key.toString() +
	 * divider + value.toString()
	 *
	 * @param divider the string divider
	 * @return the vector of strings
	 */
	public synchronized Vector<String> toStringVector(final String divider)
	{
		final Vector<String> V=new Vector<String>(size());
		for(final Object S : keySet())
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
	public synchronized void clear()
	{
		H1.clear();
		H2.clear();
	}

	/**
	 * Checks both maps for the given object, either as a key or a value.
	 * @param arg0 the object to check for
	 * @return true if found
	 */
	public synchronized boolean contains(final Object arg0)
	{
		return H1.containsKey(arg0) || H2.containsKey(arg0);
	}

	@Override
	public synchronized boolean containsKey(final Object arg0)
	{
		return H1.containsKey(arg0);
	}

	@Override
	public synchronized boolean containsValue(final Object arg0)
	{
		return H2.containsKey(arg0);
	}

	/**
	 * Returns an enumeration of the values in this map
	 *
	 * @return the enumeration of values
	 */
	public synchronized Enumeration<F> elements()
	{
		return new IteratorEnumeration<F>(H1.values().iterator());
	}

	@Override
	public synchronized Set<java.util.Map.Entry<K, F>> entrySet()
	{
		return H1.entrySet();
	}

	@Override
	public boolean equals(final Object arg0)
	{
		return this==arg0;
	}

	@Override
	public synchronized F get(final Object arg0)
	{
		return H1.get(arg0);
	}

	/**
	 * Gets the key associated with the given value
	 *
	 * @param arg0 the value to look up
	 * @return the key associated with the value
	 */
	public synchronized K getValue(final Object arg0)
	{
		return H2.get(arg0);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return H1.isEmpty();
	}

	@Override
	public synchronized Set<K> keySet()
	{
		return H1.keySet();
	}

	@Override
	public synchronized F put(final K arg0, final F arg1)
	{
		final F f=H1.put(arg0, arg1);
		if(arg1!=null)
			H2.put(arg1, arg0);
		return f;
	}

	@Override
	public synchronized F remove(final Object arg0)
	{
		final F f=H1.remove(arg0);
		if(f!=null)
			H2.remove(f);
		return f;
	}

	@Override
	public synchronized int size()
	{
		return H1.size();
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public synchronized Collection<F> values()
	{
		return new ReadOnlyCollection<F>(H1.values());
	}

	@Override
	public synchronized void putAll(final Map<? extends K, ? extends F> arg0)
	{
		if(arg0 != null)
		{
			for(final java.util.Map.Entry<? extends K, ? extends F> e : arg0.entrySet())
				put(e.getKey(),e.getValue());
		}
	}

}
