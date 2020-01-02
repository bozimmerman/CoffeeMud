package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
   Copyright 2010-2020 Bo Zimmerman

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
public class ReadOnlyList<K> implements List<K>
{
	private final List<K> list;

	public ReadOnlyList(final List<K> l)
	{
		list=l;
	}

	@Override
	public boolean add(final K arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void add(final int arg0, final K arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(final Collection<? extends K> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(final int arg0, final Collection<? extends K> arg1)
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
		return list.contains(arg0);
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		return list.containsAll(arg0);
	}

	@Override
	public K get(final int arg0)
	{
		return list.get(arg0);
	}

	@Override
	public int indexOf(final Object arg0)
	{
		return list.indexOf(arg0);
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(list.iterator());
	}

	@Override
	public int lastIndexOf(final Object arg0)
	{
		return list.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<K> listIterator()
	{
		return new ReadOnlyListIterator<K>(list.listIterator());
	}

	@Override
	public ListIterator<K> listIterator(final int arg0)
	{
		return new ReadOnlyListIterator<K>(list.listIterator(arg0));
	}

	@Override
	public boolean remove(final Object arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K remove(final int arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(final Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean retainAll(final Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K set(final int arg0, final K arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public List<K> subList(final int arg0, final int arg1)
	{
		return new ReadOnlyList<K>(list.subList(arg0,arg1));
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return list.toArray(arg0);
	}

}
