package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

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

/*
 * A version of the Vector class with better constructors
 */
public class XVector<T> extends Vector<T>
{
	private static final long	serialVersionUID	= 6687178785122563992L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final ReadOnlyList empty = new ReadOnlyList(new ArrayList());
	private String specialData = null;

	public XVector(final Collection<? extends T> V)
	{
		super((V==null)?0:V.size());
		if (V != null)
		{
			addAll(V);
			setSpecialData(V);
		}
	}

	public XVector(final List<? extends T> V, final int start)
	{
		super((V==null)?0:V.size());
		if (V != null)
		{
			for(int i=start;i<V.size();i++)
				add(V.get(i));
			setSpecialData(V);
		}
	}

	public XVector()
	{
		super();
	}

	public XVector(final int size, final boolean boo)
	{
		super(size);
	}

	public XVector(final T[] E)
	{
		super((E==null)?0:E.length);
		if (E != null)
			for (final T o : E)
				add(o);
	}

	public XVector(final T E)
	{
		super();
		if (E != null)
			add(E);
	}

	public XVector(final T E, final T E2)
	{
		this(E);
		if (E2 != null)
			add(E2);
	}

	public XVector(final T E, final T E2, final T E3)
	{
		this(E, E2);
		if (E3 != null)
			add(E3);
	}

	public XVector(final T E, final T E2, final T E3, final T E4)
	{
		this(E, E2, E3);
		if (E4 != null)
			add(E4);
	}

	public XVector(final T E, final T E2, final T E3, final T E4, final T E5)
	{
		this(E, E2, E3, E4);
		if (E5 != null)
			add(E5);
	}

	public XVector(final T E, final T E2, final T E3, final T E4, final T E5, final T E6)
	{
		this(E, E2, E3, E4, E5);
		if (E6 != null)
			add(E6);
	}

	public XVector(final Set<T> E)
	{
		super((E==null)?0:E.size());
		if (E != null)
		{
			for (final T o : E)
				add(o);
		}
	}

	public XVector(final Enumeration<T> E)
	{
		super();
		if (E != null)
		{
			for (; E.hasMoreElements();)
				add(E.nextElement());
		}
	}

	public XVector(final Iterator<T> E)
	{
		super();
		if (E != null)
		{
			for (; E.hasNext();)
				add(E.next());
		}
	}

	public void setSpecialData(final Object o)
	{
		if(o instanceof String)
			specialData = (String)o;
		if(o instanceof XVector)
		{
			@SuppressWarnings({"unchecked","rawtypes"})
			final XVector<T> x = (XVector)o;
			specialData = x.specialData;
		}
	}

	public synchronized XVector<T> append(final T[] E)
	{
		addAll(E);
		return this;
	}

	public synchronized XVector<T> append(final T E)
	{
		if(add(E))
			return this;
		return null;
	}

	public synchronized void addAll(final Enumeration<? extends T> E)
	{
		if (E != null)
		{
			for (; E.hasMoreElements();)
				add(E.nextElement());
		}
	}

	public synchronized void addAll(final T[] E)
	{
		if (E != null)
		{
			for (final T e : E)
				add(e);
		}
	}

	public synchronized void addAll(final Iterator<? extends T> E)
	{
		if (E != null)
		{
			for (; E.hasNext();)
				add(E.next());
		}
	}

	public synchronized void removeAll(final Enumeration<? extends T> E)
	{
		if (E != null)
		{
			for (; E.hasMoreElements();)
				remove(E.nextElement());
		}
	}

	public synchronized void removeAll(final Iterator<? extends T> E)
	{
		if (E != null)
		{
			for (; E.hasNext();)
				remove(E.next());
		}
	}

	public synchronized void removeAll(final List<T> E)
	{
		if (E != null)
		{
			for (final T o : E)
				remove(o);
		}
	}

	public void setComparator(final Comparator<T> comparator)
	{
		this.comparator = comparator;
	}

	/**
	 * Called from a new version of the list, this will return a set of
	 * two lists, the first being things that need adding, because they
	 * are missing from the old one, and the second list being things that
	 * need removing, because they are missing from this one.
	 *
	 * @param target the old version of the list
	 * @param comp a comparator to use for the object,
	 * @return the list pair
	 */
	public final List<T>[] makeDeltas(final List<T> target, final Comparator<T> comp)
	{
		@SuppressWarnings("unchecked")
		final List<T>[] deltas = new List[]{ new ArrayList<T>(), new ArrayList<T>() };
		final Set<T> matched = new HashSet<T>();
		for(final T t : this)
		{
			boolean found=false;
			for(final T tt : target)
			{
				if((comp.compare(t, tt)==0)
				&&(!matched.contains(tt)))
				{
					matched.add(tt);
					found=true;
					break;
				}
			}
			if(!found)
				deltas[0].add(t);
		}
		matched.clear();
		for(final T t : target)
		{
			boolean found=false;
			for(final T tt : this)
			{
				if((comp.compare(t, tt)==0)
				&&(!matched.contains(tt)))
				{
					matched.add(tt);
					found=true;
					break;
				}
			}
			if(!found)
				deltas[1].add(t);
		}
		return deltas;
	}

	public final Comparator<T> anyComparator = new Comparator<T>()
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public int compare(final T arg0, final T arg1)
		{
			if (arg0 == null)
			{
				if (arg1 == null)
					return 0;
				return -1;
			}
			else
			if (arg1 == null)
			{
				return 1;
			}
			else
			if (arg0 instanceof Comparable)
				return ((Comparable) arg0).compareTo(arg1);
			else
				return arg0.toString().compareTo(arg1.toString());
		}

	};

	public String getSpecialData()
	{
		return specialData;
	}

	protected Comparator<T> comparator = anyComparator;

	public synchronized void sort()
	{
		Collections.sort(this, comparator);
	}
}
