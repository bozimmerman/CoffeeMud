package com.planet_ink.coffee_mud.core.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;

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
public class ReadOnlyMultiList<K> implements List<K>
{
	private final List<List<K>>	lists	= new Vector<List<K>>();

	public ReadOnlyMultiList(List<K>[] esets)
	{
		if ((esets == null) || (esets.length == 0))
			return;
		for (final List<K> I : esets)
			lists.add(I);
	}

	public ReadOnlyMultiList()
	{

	}

	public void addList(List<K> eset)
	{
		lists.add(eset);
	}

	@Override
	public int size()
	{
		int size = 0;
		for (final List<K> l : lists)
			size += l.size();
		return size;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public boolean contains(Object o)
	{
		for (final List<K> l : lists)
		{
			if (l.contains(o))
				return true;
		}
		return false;
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(new MultiIterator<K>(lists));
	}

	@Override
	public Object[] toArray()
	{
		if (lists.size() > 0)
		{
			final Iterator<List<K>> iter = lists.iterator();
			Object[] array = iter.next().toArray();
			for (; iter.hasNext();)
			{
				final List<K> l = iter.next();
				if (l.size() > 0)
				{
					final int oldLen = array.length;
					array = Arrays.copyOf(array, oldLen + l.size());
					System.arraycopy(l.toArray(), 0, array, oldLen, l.size());
				}
			}
			return array;
		}
		return new Object[0];
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		if (lists.size() > 0)
		{
			final Iterator<List<K>> iter = lists.iterator();
			a = iter.next().toArray(a);
			for (; iter.hasNext();)
			{
				final List<K> l = iter.next();
				if (l.size() > 0)
				{
					final int oldLen = a.length;
					a = Arrays.copyOf(a, oldLen + l.size());
					System.arraycopy(l.toArray(), 0, a, oldLen, l.size());
				}
			}
		}
		return a;
	}

	@Override
	public boolean add(K e)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (final List<K> l : lists)
		{
			if (l.containsAll(c))
				return true;
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		lists.clear();
	}

	@Override
	public K get(int index)
	{
		for (final List<K> l : lists)
		{
			if (index < l.size())
				return l.get(index);
			index -= l.size();
		}
		throw new java.lang.IndexOutOfBoundsException();
	}

	@Override
	public K set(int index, K element)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void add(int index, K element)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public K remove(int index)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o)
	{
		int ct = 0;
		for (final List<K> l : lists)
		{
			final int x = l.indexOf(o);
			if (x >= 0)
				return ct + x;
			ct += x;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o)
	{
		int ct = size();
		for (int i = lists.size() - 1; i >= 0; i--)
		{
			final List<K> l = lists.get(i);
			ct -= l.size();
			final int x = l.lastIndexOf(o);
			if (x >= 0)
				return ct + x;
		}
		return -1;
	}

	@Override
	public ListIterator<K> listIterator()
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public ListIterator<K> listIterator(int index)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public List<K> subList(int fromIndex, int toIndex)
	{
		throw new java.lang.UnsupportedOperationException();
	}
}
