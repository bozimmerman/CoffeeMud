package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
/*
	Copyright 2015-2020 Bo Zimmerman

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
public class SafeChildList<K> implements List<K>
{
	private final List<K> list;
	private final SafeCollectionHost host;

	public SafeChildList(final List<K> list, final SafeCollectionHost host)
	{
		this.list = list;
		this.host = host;
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public boolean contains(final Object o)
	{
		return list.contains(o);
	}

	@Override
	public Iterator<K> iterator()
	{
		return new SafeFeedbackIterator<K>(list.iterator(),host);
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a)
	{
		return list.toArray(a);
	}

	@Override
	public boolean add(final K e)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean remove(final Object o)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public K get(final int index)
	{
		return list.get(index);
	}

	@Override
	public K set(final int index, final K element)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void add(final int index, final K element)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public K remove(final int index)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public int indexOf(final Object o)
	{
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o)
	{
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<K> listIterator()
	{
		return new SafeFeedbackListIterator<K>(list.listIterator(), host);
	}

	@Override
	public ListIterator<K> listIterator(final int index)
	{
		return new SafeFeedbackListIterator<K>(list.listIterator(index), host);
	}

	@Override
	public List<K> subList(final int fromIndex, final int toIndex)
	{
		return new SafeChildList<K>(list.subList(fromIndex, toIndex), host);
	}
}
