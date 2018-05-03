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
public class ReadOnlyCollection<K> implements Collection<K>
{
	private final Collection<K> col;
	public ReadOnlyCollection(final Collection<K> c)
	{
		col=c;
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
	public boolean contains(Object o)
	{
		return col.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return col.containsAll(c);
	}

	@Override
	public boolean isEmpty()
	{
		return col.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(col.iterator());
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
		return col.size();
	}

	@Override
	public Object[] toArray()
	{
		return col.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return col.toArray(a);
	}
}
