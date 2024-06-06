package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

/*
   Copyright 2019-2024 Bo Zimmerman

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
 * A version of the ArrayList class with better constructors
 */
public class XArrayList<T> extends ArrayList<T>
{
	private static final long	serialVersionUID	= 6687178785122563992L;

	public XArrayList(final Collection<? extends T> V)
	{
		super((V==null)?0:V.size());
		if (V != null)
			addAll(V);
	}

	public XArrayList()
	{
		super();
	}

	public XArrayList(final int size, final boolean boo)
	{
		super(size);
	}

	public XArrayList(final T[] E)
	{
		super((E==null)?0:E.length);
		if (E != null)
			for (final T o : E)
				add(o);
	}

	public XArrayList(final T E)
	{
		super();
		if (E != null)
			add(E);
	}

	public XArrayList(final T E, final T E2)
	{
		this(E);
		if (E2 != null)
			add(E2);
	}

	public XArrayList(final T E, final T E2, final T E3)
	{
		this(E, E2);
		if (E3 != null)
			add(E3);
	}

	public XArrayList(final T E, final T E2, final T E3, final T E4)
	{
		this(E, E2, E3);
		if (E4 != null)
			add(E4);
	}

	public XArrayList(final T E, final T E2, final T E3, final T E4, final T E5)
	{
		this(E, E2, E3, E4);
		if (E5 != null)
			add(E5);
	}

	public XArrayList(final T E, final T E2, final T E3, final T E4, final T E5, final T E6)
	{
		this(E, E2, E3, E4,E5);
		if (E6 != null)
			add(E6);
	}

	public XArrayList(final Set<T> E)
	{
		super((E==null)?0:E.size());
		if (E != null)
		{
			for (final T o : E)
				add(o);
		}
	}

	public XArrayList(final Enumeration<T> E)
	{
		super();
		if (E != null)
		{
			for (; E.hasMoreElements();)
				add(E.nextElement());
		}
	}

	public XArrayList(final Iterator<T> E)
	{
		super();
		if (E != null)
		{
			for (; E.hasNext();)
				add(E.next());
		}
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

	public synchronized void sort()
	{
		Collections.sort(this, new Comparator<T>()
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

		});
	}
}
