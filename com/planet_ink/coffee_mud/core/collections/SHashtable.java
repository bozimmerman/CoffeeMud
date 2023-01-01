package com.planet_ink.coffee_mud.core.collections;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/*
   Copyright 2010-2023 Bo Zimmerman

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
	private static final long				serialVersionUID	= 6687178785122561993L;
	private volatile Map<K, F>				H;
	private transient final ReentrantLock	lock				= new ReentrantLock();
	private volatile boolean				dirty				= false;

	public SHashtable()
	{
		super();
		H = new HashMap<K, F>();
	}

	public SHashtable(final int size)
	{
		super();
		this.H = new HashMap<K, F>(size);
	}

	public SHashtable(final Enumeration<Pair<K, F>> e)
	{
		super();
		this.H = new HashMap<K, F>();
		if (H != null)
		{
			for (; e.hasMoreElements();)
			{
				final Pair<K, F> p = e.nextElement();
				this.H.put(p.first, p.second);
			}
		}
	}

	public SHashtable(final Map<K, F> H)
	{
		super();
		if(H instanceof SHashtable)
			this.H = new HashMap<K,F>(((SHashtable<K,F>)H).H);
		else
		{
			this.H = new HashMap<K, F>();
			if (H != null)
			{
				for (final K o : H.keySet())
					this.H.put(o, H.get(o));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public SHashtable(final Object[][] H)
	{
		super();
		this.H = new HashMap<K, F>();
		if (H != null)
		{
			for (final Object[] o : H)
				this.H.put((K) o[0], (F) o[1]);
		}
	}

	@SuppressWarnings("unchecked")
	public SHashtable(final SHashtable<K, F> H, final boolean reverse)
	{
		super();
		this.H = new HashMap<K, F>();
		if (H != null)
		{
			if(reverse)
			{
				for (final K o : H.keySet())
					this.H.put((K)H.get(o), (F)o);
			}
			else
			{
				this.H.putAll(H);
			}
		}
	}

	public SHashtable(final SHashtable<K, F> H)
	{
		this(H,false);
	}

	public Map<K, F> toHashtable()
	{
		return new HashMap<K,F>(H);
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public List<String> toStringVector(final String divider)
	{
		final List<String> V = Collections.synchronizedList(new ArrayList<String>(size()));
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

	@Override
	public void clear()
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		try
		{
			if(H.size()==0)
				return;
			this.H = new HashMap<K, F>();
		}
		finally
		{
			lock.unlock();
		}
	}

	public SHashtable<K, F> copyOf()
	{
		return new SHashtable<K, F>(H);
	}

	public boolean contains(final Object arg0)
	{
		return H.containsValue(arg0);
	}

	@Override
	public boolean containsKey(final Object arg0)
	{
		return H.containsKey(arg0);
	}

	@Override
	public boolean containsValue(final Object arg0)
	{
		return H.containsValue(arg0);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<F> elements()
	{
		if(size()==0)
			return EmptyEnumeration.INSTANCE;
		return new IteratorEnumeration<F>(H.values().iterator());
	}

	@Override
	public Set<java.util.Map.Entry<K, F>> entrySet()
	{
		return H.entrySet();
	}

	@Override
	public boolean equals(final Object arg0)
	{
		return this == arg0;
	}

	@Override
	public F get(final Object arg0)
	{
		return H.get(arg0);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean isEmpty()
	{
		return H.isEmpty();
	}

	public Enumeration<K> keys()
	{
		return new IteratorEnumeration<K>(H.keySet().iterator());
	}

	@Override
	public Set<K> keySet()
	{
		return H.keySet();
	}

	@Override
	public F put(final K arg0, final F arg1)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final HashMap<K, F> H2 = new HashMap<K, F>(H);
		try
		{
			dirty=true;
			return H2.put(arg0, arg1);
		}
		finally
		{
			this.H = H2;
			lock.unlock();
		}
	}

	@Override
	public F remove(final Object arg0)
	{
		if(!H.containsKey(arg0))
			return null;
		final ReentrantLock lock = this.lock;
		lock.lock();
		final HashMap<K, F> H2 = new HashMap<K, F>(H);
		try
		{
			dirty=true;
			return H2.remove(arg0);
		}
		finally
		{
			this.H = H2;
			lock.unlock();
		}
	}

	@Override
	public int size()
	{
		return H.size();
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public Collection<F> values()
	{
		return new ReadOnlyCollection<F>(H.values());
	}

	@Override
	public void putAll(final Map<? extends K, ? extends F> arg0)
	{
		final ReentrantLock lock = this.lock;
		lock.lock();
		final HashMap<K, F> H2 = new HashMap<K, F>(H);
		try
		{
			dirty=true;
			H2.putAll(arg0);
		}
		finally
		{
			this.H = H2;
			lock.unlock();
		}
	}

}
