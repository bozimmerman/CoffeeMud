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
public class ReadOnlyListSet<K> implements Set<K>
{
	private final Object[]	array;
	private final Set<K>	set;

	public ReadOnlyListSet()
	{
		set = new TreeSet<K>();
		array = new Object[0];
	}

	public ReadOnlyListSet(Set<K> s)
	{
		set = s;
		array = s.toArray(new Object[0]);
	}

	@Override
	public boolean add(K e)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(Collection<? extends K> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean contains(Object arg0)
	{
		return set.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		return set.containsAll(arg0);
	}

	@Override
	public boolean isEmpty()
	{
		return set.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(new Iterator<K>() 
		{
			private int	d	= 0;

			@Override
			public boolean hasNext()
			{
				return d < array.length;
			}

			@SuppressWarnings("unchecked")

			@Override
			public K next()
			{
				if (hasNext())
				{
					final K o = (K) array[d];
					d++;
					return o;
				}
				return null;
			}

			@Override
			public void remove()
			{
				throw new java.lang.IllegalArgumentException();
			}
		});
	}

	@Override
	public boolean remove(Object o)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return array.length;
	}

	@Override
	public Object[] toArray()
	{
		return array;
	}

	@Override
	public <T> T[] toArray(T[] arg0)
	{
		return set.toArray(arg0);
	}
}
