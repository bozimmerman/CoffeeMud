package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
   Copyright 2013-2018 Bo Zimmerman

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
public class DoubleMap<K,F> implements java.util.Map<K,F>, java.io.Serializable
{
	private static final long serialVersionUID = 6687178785122561993L;
	private volatile Map<K,F> H1;
	private volatile Map<F,K> H2;
	public DoubleMap(Map<K,F> map1, Map<F,K> map2)
	{
		super();
		H1=map1;
		H2=map2;
	}

	public synchronized Vector<String> toStringVector(String divider)
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

	public synchronized boolean contains(Object arg0)
	{
		return H1.containsKey(arg0) || H2.containsKey(arg0);
	}

	@Override
	public synchronized boolean containsKey(Object arg0)
	{
		return H1.containsKey(arg0);
	}

	@Override
	public synchronized boolean containsValue(Object arg0)
	{
		return H2.containsKey(arg0);
	}

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
	public boolean equals(Object arg0)
	{
		return this==arg0;
	}

	@Override
	public synchronized F get(Object arg0)
	{
		return H1.get(arg0);
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
	public synchronized F put(K arg0, F arg1)
	{
		final F f=H1.put(arg0, arg1);
		if(f!=null)
			H2.put(arg1, arg0);
		return f;
	}

	@Override
	public synchronized F remove(Object arg0)
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
	public synchronized void putAll(Map<? extends K, ? extends F> arg0)
	{
		if(arg0 != null)
		{
			for(final java.util.Map.Entry<? extends K, ? extends F> e : arg0.entrySet())
				put(e.getKey(),e.getValue());
		}
	}

}
