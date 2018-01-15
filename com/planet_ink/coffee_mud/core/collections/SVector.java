package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.OperationNotSupportedException;

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

/*
 * A version of the CopyOnWriteArrayList class that provides to "safe" adds
 * and removes by copying the underlying CopyOnWriteArrayList whenever those
 * operations are done.
 */
public class SVector<T> extends CopyOnWriteArrayList<T> implements Serializable, Iterable<T>, Collection<T>, CList<T>, RandomAccess
{
	private static final long	serialVersionUID	= 6687178785122561992L;
	private List<T> me = Collections.synchronizedList(this);

	public SVector()
	{
		super();
	}

	public SVector(int size)
	{
		super();
	}

	public SVector(List<T> T)
	{
		super();
		if (T != null)
			this.addAll(T);
	}

	public SVector(T[] T)
	{
		super();
		if (T != null)
		{
			for (final T o : T)
				add(o);
		}
	}

	public SVector(Enumeration<T> T)
	{
		super();
		if (T != null)
		{
			for (; T.hasMoreElements();)
				me.add(T.nextElement());
		}
	}

	public SVector(Iterator<T> T)
	{
		super();
		if (T != null)
		{
			for (; T.hasNext();)
				me.add(T.next());
		}
	}

	public SVector(Set<T> T)
	{
		super();
		if (T != null)
		{
			for (final T o : T)
				me.add(o);
		}
	}

	public void addAll(Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				me.add(T.nextElement());
		}
	}

	public void addAll(T[] T)
	{
		if (T != null)
		{
			for (final T e : T)
				me.add(e);
		}
	}

	public void addAll(Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				me.add(T.next());
		}
	}

	public void removeAll(Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				me.remove(T.nextElement());
		}
	}

	public void removeAll(Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				me.remove(T.next());
		}
	}

	public void removeAll(List<T> T)
	{
		if (T != null)
		{
			for (final T o : T)
				me.remove(o);
		}
	}

	public int capacity()
	{
		return me.size();
	}

	public Vector<T> toVector()
	{
		return new XVector<T>(me);
	}

	@SuppressWarnings("unchecked")
	public SVector<T> copyOf()
	{
		try
		{
			SVector<T> copy= (SVector<T>) clone();
			copy.me = Collections.synchronizedList(copy);
			return copy;
		}
		catch (final Exception e)
		{
			return new SVector<T>(me);
		}
	}

	public void copyInto(Object[] anArray)
	{
		me.toArray(anArray);
	}

	public T elementAt(int index)
	{
		return me.get(index);
	}

	public Enumeration<T> elements()
	{
		return new Enumeration<T>() 
		{
			final Iterator<T>	i	= me.iterator();

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

	public void ensureCapacity(int minCapacity)
	{
		throw new IllegalArgumentException();
	}

	public T firstElement()
	{
		return (me.size() == 0) ? null : me.get(0);
	}

	public T lastElement()
	{
		return (me.size() == 0) ? null : me.get(size() - 1);
	}

	public void setElementAt(T obj, int index)
	{
		me.set(index, obj);
	}

	public void setSize(int newSize)
	{
		if (newSize == 0)
			me.clear();
		else
			throw new IllegalArgumentException();
	}

	public void trimToSize()
	{
	}

	public void addElement(T obj)
	{
		me.add(obj);
	}

	public void insertElementAt(T obj, int index)
	{
		me.add(index, obj);
	}

	public void removeAllElements()
	{
		me.clear();
	}

	@Override
	public int size()
	{
		return me.size();
	}
	
	@Override
	public boolean isEmpty()
	{
		return me.isEmpty();
	}
	
	@Override
	public boolean contains(Object o)
	{
		return me.contains(o);
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return me.iterator();
	}
	
	@Override
	public Object[] toArray()
	{
		return me.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a)
	{
		return me.toArray(a);
	}
	
	@Override
	public boolean add(T e)
	{
		return me.add(e);
	}
	
	@Override
	public boolean remove(Object o)
	{
		return me.remove(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		return me.containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		return me.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		return me.addAll(c);
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		return me.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		return me.retainAll(c);
	}
	
	@Override
	public void clear()
	{
		me.clear();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return me.equals(o);
	}
	
	@Override
	public int hashCode()
	{
		return me.hashCode();
	}
	
	@Override
	public T get(int index)
	{
		return me.get(index);
	}
	
	@Override
	public T set(int index, T element)
	{
		return me.set(index, element);
	}
	
	@Override
	public void add(int index, T element)
	{
		me.add(index,element);
	}
	
	@Override
	public T remove(int index)
	{
		return me.remove(index);
	}
	
	@Override
	public int indexOf(Object o)
	{
		return me.indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o)
	{
		return me.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<T> listIterator()
	{
		return me.listIterator();
	}
	
	@Override
	public ListIterator<T> listIterator(int index)
	{
		return me.listIterator(index);
	}
	
	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		return me.subList(fromIndex, toIndex);
	}
}
