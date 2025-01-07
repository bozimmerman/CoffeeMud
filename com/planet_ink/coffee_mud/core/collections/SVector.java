package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.OperationNotSupportedException;

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
 * A version of the CopyOnWriteArrayList class that provides to "safe" adds
 * and removes by copying the underlying CopyOnWriteArrayList whenever those
 * operations are done.
 */
public class SVector<T> implements Serializable, Iterable<T>, Collection<T>, CList<T>, RandomAccess
{
	private static final long	serialVersionUID	= 6687178785122561992L;

	private CopyOnWriteArrayList<T> underList = new CopyOnWriteArrayList<T>();

	private List<T> list = Collections.synchronizedList(underList);

	public SVector()
	{
		super();
	}

	public SVector(final int size)
	{
		super();
	}

	public SVector(final List<T> T)
	{
		super();
		if (T != null)
			addAll(T);
	}

	public SVector(final T[] T)
	{
		super();
		if (T != null)
		{
			for (final T o : T)
				add(o);
		}
	}

	public SVector(final Enumeration<T> T)
	{
		super();
		if (T != null)
		{
			for (; T.hasMoreElements();)
				add(T.nextElement());
		}
	}

	public SVector(final Iterator<T> T)
	{
		super();
		if (T != null)
		{
			for (; T.hasNext();)
				add(T.next());
		}
	}

	public SVector(final Set<T> T)
	{
		super();
		if (T != null)
		{
			for (final T o : T)
				add(o);
		}
	}

	public void addAll(final Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				add(T.nextElement());
		}
	}

	public void addAll(final T[] T)
	{
		if (T != null)
		{
			for (final T e : T)
				add(e);
		}
	}

	public void addAll(final Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				add(T.next());
		}
	}

	public void removeAll(final Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				remove(T.nextElement());
		}
	}

	public void removeAll(final Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				remove(T.next());
		}
	}

	public void removeAll(final List<T> T)
	{
		if (T != null)
		{
			for (final T o : T)
				remove(o);
		}
	}

	public int capacity()
	{
		return size();
	}

	public Vector<T> toVector()
	{
		return new XVector<T>(list);
	}

	@SuppressWarnings("unchecked")
	public SVector<T> copyOf()
	{
		try
		{
			synchronized(this)
			{
				final SVector<T> copy = new SVector<T>();
				copy.underList = (CopyOnWriteArrayList<T>)underList.clone();
				copy.list = Collections.synchronizedList(copy.underList);
				return copy;
			}
		}
		catch (final Exception e)
		{
			return new SVector<T>(list);
		}
	}

	@Override
	public SVector<T> clone()
	{
		return copyOf();
	}

	public void copyInto(final Object[] anArray)
	{
		toArray(anArray);
	}

	public T elementAt(final int index)
	{
		return get(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<T> elements()
	{
		if(list.size()==0)
			return EmptyEnumeration.INSTANCE;
		return new Enumeration<T>()
		{
			final Iterator<T>	i	= iterator();

			@Override
			public boolean hasMoreElements()
			{
				return i.hasNext();
			}

			@Override
			public T nextElement()
			{
				return i.next();
			}
		};
	}

	public void ensureCapacity(final int minCapacity)
	{
		throw new IllegalArgumentException();
	}

	public T firstElement()
	{
		return (size() == 0) ? null : get(0);
	}

	public T lastElement()
	{
		return (size() == 0) ? null : get(size() - 1);
	}

	public void setElementAt(final T obj, final int index)
	{
		set(index, obj);
	}

	public void setSize(final int newSize)
	{
		if (newSize == 0)
			clear();
		else
			throw new IllegalArgumentException();
	}

	@Override
	public void trimToSize()
	{
	}

	public void addElement(final T obj)
	{
		add(obj);
	}

	public void insertElementAt(final T obj, final int index)
	{
		add(index, obj);
	}

	public void removeAllElements()
	{
		clear();
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
	public Iterator<T> iterator()
	{
		return list.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(final T[] a)
	{
		return list.toArray(a);
	}

	@Override
	public boolean add(final T e)
	{
		return list.add(e);
	}

	@Override
	public boolean remove(final Object o)
	{
		return list.remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends T> c)
	{
		return list.addAll(c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> c)
	{
		return list.addAll(c);
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return list.retainAll(c);
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public boolean equals(final Object o)
	{
		return list.equals(o);
	}

	@Override
	public int hashCode()
	{
		return list.hashCode();
	}

	@Override
	public T get(final int index)
	{
		return list.get(index);
	}

	public T getSafe(final int index)
	{
		try
		{
			return list.get(index);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	@Override
	public T set(final int index, final T element)
	{
		return list.set(index, element);
	}

	@Override
	public void add(final int index, final T element)
	{
		list.add(index,element);
	}

	@Override
	public T remove(final int index)
	{
		return list.remove(index);
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
	public ListIterator<T> listIterator()
	{
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(final int index)
	{
		return list.listIterator(index);
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex)
	{
		return list.subList(fromIndex, toIndex);
	}

	public boolean removeElement(final Object obj)
	{
		return list.remove(obj);
	}

	public void removeElementAt(final int index)
	{
		list.remove(index);
	}
}
