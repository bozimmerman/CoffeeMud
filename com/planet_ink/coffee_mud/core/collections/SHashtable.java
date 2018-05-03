package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

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
public class SHashtable<K, F> implements CMap<K, F>, java.io.Serializable
{
	private static final long	     serialVersionUID	= 6687178785122561993L;
	private volatile Hashtable<K, F>	H;

	public SHashtable()
	{
		super();
		H = new Hashtable<K, F>();
	}

	public SHashtable(int size)
	{
		super();
		H = new Hashtable<K, F>(size);
	}

	public SHashtable(Enumeration<Pair<K, F>> e)
	{
		super();
		this.H = new Hashtable<K, F>();
		if (H != null)
		{
			for (; e.hasMoreElements();)
			{
				final Pair<K, F> p = e.nextElement();
				put(p.first, p.second);
			}
		}
	}

	public SHashtable(Map<K, F> H)
	{
		super();
		this.H = new Hashtable<K, F>();
		if (H != null)
		{
			for (final K o : H.keySet())
				put(o, H.get(o));
		}
	}

	@SuppressWarnings("unchecked")
	public SHashtable(Object[][] H)
	{
		super();
		this.H = new Hashtable<K, F>();
		if (H != null)
		{
			for (final Object[] o : H)
				this.H.put((K) o[0], (F) o[1]);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized Hashtable<K, F> toHashtable()
	{
		return (Hashtable<K, F>) H.clone();
	}

	public synchronized Vector<String> toStringVector(String divider)
	{
		final Vector<String> V = new Vector<String>(size());
		for (final Object S : keySet())
		{
			if (S != null)
			{
				final Object O = get(S);
				if (O == null)
					V.add(S.toString() + divider);
				else
					V.add(S.toString() + divider + O.toString());
			}
		}
		return V;
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void clear()
	{
		H = (Hashtable<K, F>) H.clone();
		H.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized SHashtable<K, F> copyOf()
	{
		final SHashtable<K, F> SH = new SHashtable<K, F>();
		SH.H = (Hashtable<K, F>) H.clone();
		return SH;
	}

	public synchronized boolean contains(Object arg0)
	{
		return H.contains(arg0);
	}

	@Override
	public synchronized boolean containsKey(Object arg0)
	{
		return H.containsKey(arg0);
	}

	@Override
	public synchronized boolean containsValue(Object arg0)
	{
		return H.containsValue(arg0);
	}

	public synchronized Enumeration<F> elements()
	{
		return H.elements();
	}

	@Override
	public synchronized Set<java.util.Map.Entry<K, F>> entrySet()
	{
		return H.entrySet();
	}

	@Override
	public boolean equals(Object arg0)
	{
		return this == arg0;
	}

	@Override
	public synchronized F get(Object arg0)
	{
		return H.get(arg0);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return H.isEmpty();
	}

	public synchronized Enumeration<K> keys()
	{
		return H.keys();
	}

	@Override
	public synchronized Set<K> keySet()
	{
		return H.keySet();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized F put(K arg0, F arg1)
	{
		H = (Hashtable<K, F>) H.clone();
		return H.put(arg0, arg1);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized F remove(Object arg0)
	{
		H = (Hashtable<K, F>) H.clone();
		return H.remove(arg0);
	}

	@Override
	public synchronized int size()
	{
		return H.size();
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public synchronized Collection<F> values()
	{
		return new ReadOnlyCollection<F>(H.values());
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void putAll(Map<? extends K, ? extends F> arg0)
	{
		H = (Hashtable<K, F>) H.clone();
		H.putAll(arg0);
	}

}
