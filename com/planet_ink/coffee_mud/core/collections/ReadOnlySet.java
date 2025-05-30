package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2010-2025 Bo Zimmerman

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
public class ReadOnlySet<K> implements Set<K>
{
	private final Set<K>	set;

	public ReadOnlySet()
	{
		set = new HashSet<K>();
	}

	public ReadOnlySet(final Set<K> s)
	{
		set = s;
	}

	@Override
	public boolean add(final K e)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean contains(final Object arg0)
	{
		return set.contains(arg0);
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
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
		return new ReadOnlyIterator<K>(set.iterator());
	}

	@Override
	public boolean remove(final Object o)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public Object[] toArray()
	{
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return set.toArray(arg0);
	}
}
