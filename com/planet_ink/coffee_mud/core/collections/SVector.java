package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.OperationNotSupportedException;

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

/***
 * A version of the CopyOnWriteArrayList class that provides to "safe" adds
 * and removes by copying the underlying CopyOnWriteArrayList whenever those
 * operations are done.
 *
 * Actual Contract:
 * 1. Thread safe add/remove
 * 2. Iterator that is a snapshot of the moment it is called, and does not change afterwards.
 *
 * @param <T> the type of object contained in the list
 */
public class SVector<T> implements Serializable, Iterable<T>, Collection<T>, CList<T>, RandomAccess
{
	private static final long	serialVersionUID	= 6687178785122561992L;

	private CopyOnWriteArrayList<T> underList = new CopyOnWriteArrayList<T>();

	private List<T> list = Collections.synchronizedList(underList);

	/**
	 * Construct an empty synchronized vector.
	 */
	public SVector()
	{
		super();
	}

	/**
	 * Construct an empty synchronized vector with the specified initial
	 * capacity.
	 *
	 * @param size the initial capacity of the vector
	 */
	public SVector(final int size)
	{
		super();
	}

	/**
	 * Construct a synchronized vector containing the elements of the specified
	 * collection, in the order they are returned by the collection's iterator.
	 *
	 * @param T the collection whose elements are to be placed into this vector
	 */
	public SVector(final List<T> T)
	{
		super();
		if (T != null)
			addAll(T);
	}

	/**
	 * Construct a synchronized vector containing the elements of the specified
	 * array, in the order they are returned by the array's iterator.
	 *
	 * @param T the array whose elements are to be placed into this vector
	 */
	public SVector(final T[] T)
	{
		super();
		if (T != null)
		{
			for (final T o : T)
				add(o);
		}
	}

	/**
	 * Construct a synchronized vector containing the elements of the specified
	 * enumeration, in the order they are returned by the enumeration's
	 * iterator.
	 *
	 * @param T the enumeration whose elements are to be placed into this vector
	 */
	public SVector(final Enumeration<T> T)
	{
		super();
		if (T != null)
		{
			for (; T.hasMoreElements();)
				add(T.nextElement());
		}
	}

	/**
	 * Construct a synchronized vector containing the elements of the specified
	 * iterator, in the order they are returned by the iterator's iterator.
	 *
	 * @param T the iterator whose elements are to be placed into this vector
	 */
	public SVector(final Iterator<T> T)
	{
		super();
		if (T != null)
		{
			for (; T.hasNext();)
				add(T.next());
		}
	}

	/**
	 * Construct a synchronized vector containing the elements of the specified
	 * set, in the order they are returned by the set's iterator.
	 *
	 * @param T the set whose elements are to be placed into this vector
	 */
	public SVector(final Set<T> T)
	{
		super();
		if (T != null)
		{
			for (final T o : T)
				add(o);
		}
	}

	/**
	 * Add all the elements of the given enumeration to this vector.
	 *
	 * @param T the collection to add
	 */
	public void addAll(final Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				add(T.nextElement());
		}
	}

	/**
	 * Add all the elements of the given list to this vector.
	 *
	 * @param T the collection to add
	 */
	public void addAll(final T[] T)
	{
		if (T != null)
		{
			for (final T e : T)
				add(e);
		}
	}

	/**
	 * Add all the elements of the given Iterator to this vector.
	 *
	 * @param T the Iterator to add
	 */
	public void addAll(final Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				add(T.next());
		}
	}

	/**
	 * Removes all the elements of the given Enumeration from this vector.
	 *
	 * @param T the collection to remove
	 */
	public void removeAll(final Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				remove(T.nextElement());
		}
	}

	/**
	 * Removes all the elements of the given Iterator from this vector.
	 *
	 * @param T the Iterator to remove
	 */
	public void removeAll(final Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				remove(T.next());
		}
	}

	/**
	 * Removes all the elements of the given list from this vector.
	 *
	 * @param T the collection to remove
	 */
	public void removeAll(final List<T> T)
	{
		if (T != null)
		{
			for (final T o : T)
				remove(o);
		}
	}

	/**
	 * Returns the current capacity of this vector.  Since this vector
	 * grows as needed, this method simply returns the current size.
	 *
	 * @return the current capacity of this vector
	 */
	public int capacity()
	{
		return size();
	}

	/**
	 * Returns a non-synchronized version of this vector.
	 *
	 * @return a non-synchronized version of this vector
	 */
	public Vector<T> toVector()
	{
		return new XVector<T>(list);
	}

	/**
	 * Returns a shallow copy of this vector. The elements themselves are not
	 * cloned.
	 *
	 * @return a shallow copy of this vector
	 */
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

	/**
	 * Copies the components of this vector into the specified array. The array
	 * must be big enough to hold all the objects in this vector, or an
	 * ArrayIndexOutOfBoundsException will be thrown.
	 *
	 * @param anArray the array into which the components get copied
	 */
	public void copyInto(final Object[] anArray)
	{
		toArray(anArray);
	}

	/**
	 * Returns the component at the specified index.
	 *
	 * @param index the desired index
	 * @return the component at the specified index
	 */
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

	/**
	 * Throws IllegalArgumentException, as this vector grows as needed.
	 *
	 * @param minCapacity the desired minimum capacity
	 */
	public void ensureCapacity(final int minCapacity)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Returns the first component of this vector.
	 * @return the first component of this vector
	 */
	public T firstElement()
	{
		return (size() == 0) ? null : get(0);
	}

	/**
	 * Returns the last component of this vector.
	 * @return the last component of this vector
	 */
	public T lastElement()
	{
		return (size() == 0) ? null : get(size() - 1);
	}

	/**
	 * Sets the component at the specified index of this vector to be the
	 * specified object. The previous component at that position is discarded.
	 *
	 * @param obj the object to set
	 * @param index the desired index
	 */
	public void setElementAt(final T obj, final int index)
	{
		set(index, obj);
	}

	/**
	 * Clears the vector if the new size is zero, otherwise throws
	 * IllegalArgumentException, as this vector grows as needed.
	 *
	 * @param newSize the new desired size of the vector
	 */
	public void setSize(final int newSize)
	{
		if (newSize == 0)
			clear();
		else
			throw new IllegalArgumentException();
	}

	/**
	 * Does nothing, as this vector grows as needed.
	 */
	@Override
	public void trimToSize()
	{
	}

	/**
	 * Adds the specified object to the end of this vector.
	 *
	 * @param obj the object to add
	 */
	public void addElement(final T obj)
	{
		add(obj);
	}

	/**
	 * Inserts the specified object as a component in this vector at the
	 * specified index. Each component in this vector with an index greater or
	 * equal to the specified index is shifted upward to have an index one
	 * greater than the value it had previously.
	 *
	 * @param obj the object to insert
	 * @param index the desired index
	 */
	public void insertElementAt(final T obj, final int index)
	{
		add(index, obj);
	}

	/**
	 * Removes all components from this vector and sets its size to zero.
	 */
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

	/**
	 * Gets the element at the given index if it exists, or null if it does not.
	 * This method never throws an exception.
	 *
	 * @param index the index to get
	 * @return the object at that index, or null
	 */
	public T getSafe(final int index)
	{
		try
		{
			return list.get(index);
		}
		catch(final Exception e)
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

	/**
	 * An alias for remove(Object)
	 *
	 * @param obj the object to remove
	 * @return true if it was removed
	 */
	public boolean removeElement(final Object obj)
	{
		return list.remove(obj);
	}

	/**
	 * An alias for remove(int)
	 *
	 * @param index the index to remove
	 */
	public void removeElementAt(final int index)
	{
		list.remove(index);
	}
}
