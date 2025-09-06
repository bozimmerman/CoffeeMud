package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2019-2025 Bo Zimmerman

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

/**
 * A simple ArrayList implementation of a PairList
 * @param <T> the type of the first element
 * @param <K> the type of the second element
 */
public class PairArrayList<T, K> extends ArrayList<Pair<T, K>> implements PairList<T, K>
{
	/**
	 *
	 */
	private static final long	serialVersionUID	= 1672867955945287259L;

	/**
	 * Creates a new PairArrayList containing the elements of the given initial
	 * list.
	 *
	 * @param initial the initial list
	 */
	public PairArrayList(final List<Pair<T,K>> initial)
	{
		super(initial.size());
		addAll(initial);
	}

	/**
	 * Creates a new empty PairArrayList.
	 */
	public PairArrayList()
	{
		super();
	}

	/**
	 * Creates a new empty PairArrayList of the given initial capacity.
	 *
	 * @param x the initial capacity
	 */
	public PairArrayList(final int x)
	{
		super(x);
	}

	/**
	 * A converter that converts a Pair to its first value.
	 *
	 * @return the first converter
	 */
	@Override
	public Pair.FirstConverter<T, K> getFirstConverter()
	{
		return new Pair.FirstConverter<T, K>();
	}

	/**
	 * A converter that converts a Pair to its second value.
	 *
	 * @return the second converter
	 */
	@Override
	public Pair.SecondConverter<T, K> getSecondConverter()
	{
		return new Pair.SecondConverter<T, K>();
	}

	/**
	 * Returns an enumeration of the first elements in the list.
	 *
	 * @return the enumeration of first elements
	 */
	public Enumeration<T> firstElements()
	{
		return new ConvertingEnumeration<Pair<T, K>, T>(elements(), getFirstConverter());
	}

	/**
	 * Returns an enumeration of the second elements in the list.
	 *
	 * @return the enumeration of second elements
	 */
	public Enumeration<K> secondElements()
	{
		return new ConvertingEnumeration<Pair<T, K>, K>(elements(), getSecondConverter());
	}

	/**
	 * Returns an enumeration of the pairs in the list.
	 *
	 * @return the enumeration of pairs
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<Pair<T,K>> elements()
	{
		if(size()==0)
			return EmptyEnumeration.INSTANCE;
		return new IteratorEnumeration<Pair<T,K>>(iterator());
	}

	/**
	 * Returns an iterator of the first elements in the list.
	 *
	 * @return the iterator of first elements
	 */
	@Override
	public Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Pair<T, K>, T>(iterator(), getFirstConverter());
	}

	/**
	 * Returns an iterator of the second elements in the list.
	 *
	 * @return the iterator of second elements
	 */
	@Override
	public Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Pair<T, K>, K>(iterator(), getSecondConverter());
	}

	/**
	 * Returns the index of the first occurrence of the given first element,
	 *
	 * @param t the first element to look for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	@Override
	public synchronized int indexOfFirst(final T t)
	{
		return indexOfFirst(t, 0);
	}

	/**
	 * Returns the index of the first occurrence of the given second element,
	 *
	 * @param k the second element to look for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	@Override
	public synchronized int indexOfSecond(final K k)
	{
		return indexOfSecond(k, 0);
	}

	/**
	 * Returns the first element of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the first element of the pair
	 */
	@Override
	public T getFirst(final int index)
	{
		return get(index).first;
	}

	/**
	 * Returns the second element of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the second element of the pair
	 */
	@Override
	public K getSecond(final int index)
	{
		return get(index).second;
	}

	/**
	 * Adds a new pair to the list, made up of the given first and second
	 * elements.
	 *
	 * @param t the first element
	 * @param k the second element
	 */
	@Override
	public void add(final T t, final K k)
	{
		add(new Pair<T, K>(t, k));
	}

	/**
	 * Adds a new pair to the list, made up of the given first and second
	 * elements.
	 *
	 * @param t the first element
	 * @param k the second element
	 */
	public void addElement(final T t, final K k)
	{
		add(new Pair<T, K>(t, k));
	}

	/**
	 * Adds a new pair to the list at the given index, made up of the given
	 * first and second elements.
	 *
	 * @param x the index to add the new pair at
	 * @param t the first element
	 * @param k the second element
	 */
	@Override
	public void add(final int x, final T t, final K k)
	{
		add(x,new Pair<T, K>(t, k));
	}

	/**
	 * Adds a new pair to the list at the given index, made up of the given
	 * first and second elements.
	 *
	 * @param x the index to add the new pair at
	 * @param t the first element
	 * @param k the second element
	 */
	public void addElement(final int x, final T t, final K k)
	{
		add(x, new Pair<T, K>(t, k));
	}

	/**
	 * Returns true if the list contains the given pair, or if it contains a
	 * pair with the given first element, or if it contains a pair with the
	 * given second element.
	 *
	 * @param o the pair, or first element, or second element to look for
	 * @return true if found, false otherwise
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final Object o)
	{
		if (o instanceof Pair)
			return super.contains(o);
		if (containsFirst((T) o))
			return true;
		return containsSecond((K) o);
	}

	/**
	 * Returns the index of the first occurrence of the given pair, or the index
	 * of the first occurrence of a pair with the given first element, or the
	 * index of the first occurrence of a pair with the given second element.
	 *
	 * @param o the pair, or first element, or second element to look for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int indexOf(final Object o)
	{
		if (o instanceof Pair)
			return super.indexOf(o);
		final int x = indexOfFirst((T) o);
		if (x >= 0)
			return x;
		return indexOfSecond((K) o);
	}

	/**
	 * Returns whether the list contains a pair with the given first element.
	 *
	 * @param t the first element to look for
	 * @return true if found, false otherwise
	 */
	@Override
	public boolean containsFirst(final T t)
	{
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			if ((t == null) ? i.next() == null : t.equals(i.next().first))
				return true;
		}
		return false;
	}

	/**
	 * Returns whether the list contains a pair with the given second element.
	 *
	 * @param k the second element to look for
	 * @return true if found, false otherwise
	 */
	@Override
	public boolean containsSecond(final K k)
	{
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			if ((k == null) ? i.next() == null : k.equals(i.next().second))
				return true;
		}
		return false;
	}

	/**
	 * Returns the first element of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the first element of the pair
	 */
	@Override
	public T elementAtFirst(final int index)
	{
		return get(index).first;
	}

	/**
	 * Returns the second element of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the second element of the pair
	 */
	@Override
	public K elementAtSecond(final int index)
	{
		return get(index).second;
	}

	/**
	 * Returns the index of the first occurrence of a pair with the given first
	 * element, starting at the given index.
	 *
	 * @param t     the first element to look for
	 * @param index the index to start searching at
	 * @return the index of the first occurrence, or -1 if not found
	 */
	@Override
	public int indexOfFirst(final T t, final int index)
	{
		try
		{
			for (int i = index; i < size(); i++)
			{
				if ((t == null ? get(i).first == null : t.equals(get(i).first)))
					return i;
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of a pair with the given second
	 * element, starting at the given index.
	 *
	 * @param k the second element to look for
	 * @param index the index to start searching at
	 * @return the index of the first occurrence, or -1 if not found
	 */
	@Override
	public int indexOfSecond(final K k, final int index)
	{
		try
		{
			for (int i = index; i < size(); i++)
			{
				if ((k == null ? get(i).second == null : k.equals(get(i).second)))
					return i;
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of a pair with the given first
	 * element.
	 *
	 * @param t the first element to look for
	 * @return the index of the last occurrence, or -1 if not found
	 */
	@Override
	public int lastIndexOfFirst(final T t, final int index)
	{
		try
		{
			for (int i = index; i >= 0; i--)
			{
				if ((t == null ? get(i).first == null : t.equals(get(i).first)))
					return i;
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of a pair with the given second
	 * element.
	 *
	 * @param k the second element to look for
	 * @return the index of the last occurrence, or -1 if not found
	 */
	@Override
	public int lastIndexOfSecond(final K k, final int index)
	{
		try
		{
			for (int i = index; i >= 0; i--)
			{
				if ((k == null ? get(i).second == null : k.equals(get(i).second)))
					return i;
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of a pair with the given first
	 * element.
	 *
	 * @param t the first element to look for
	 * @return the index of the last occurrence, or -1 if not found
	 */
	@Override
	public int lastIndexOfFirst(final T t)
	{
		return lastIndexOfFirst(t, size() - 1);
	}

	/**
	 * Returns the index of the last occurrence of a pair with the given second
	 * element.
	 *
	 * @param k the second element to look for
	 * @return the index of the last occurrence, or -1 if not found
	 */
	@Override
	public int lastIndexOfSecond(final K k)
	{
		return lastIndexOfSecond(k, size() - 1);
	}

	/**
	 * Removes the first occurrence of a pair with the given first element.
	 *
	 * @param t the first element to look for
	 * @return true if found and removed, false otherwise
	 */
	@Override
	public boolean removeFirst(final T t)
	{
		Pair<T, K> pair;
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			pair = i.next();
			if ((t == null ? pair.first == null : t.equals(pair.first)))
			{
				i.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the first occurrence of a pair with the given second element.
	 *
	 * @param k the second element to look for
	 * @return true if found and removed, false otherwise
	 */
	@Override
	public boolean removeSecond(final K k)
	{
		Pair<T, K> pair;
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			pair = i.next();
			if ((k == null ? pair.second == null : k.equals(pair.second)))
			{
				i.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the first occurrence of the element with the given first
	 * value.
	 *
	 * @param t the first element to look for
	 * @return true if found and removed, false otherwise
	 */
	@Override
	public boolean removeElementFirst(final T t)
	{
		return removeFirst(t);
	}

	/**
	 * Removes the first occurrence of the element with the given second value.
	 *
	 * @param k the second element to look for
	 * @return true if found and removed, false otherwise
	 */
	@Override
	public boolean removeElementSecond(final K k)
	{
		return removeSecond(k);
	}

	/**
	 * Returns the first pair in the list.
	 *
	 * @return the first pair
	 */
	public Pair<T,K> firstElement()
	{
		if(size()==0)
			throw new IndexOutOfBoundsException ();
		return get(0);
	}

	/**
	 * Returns the last pair in the list.
	 *
	 * @return the last pair
	 */

	public Pair<T,K> lastElement()
	{
		if(size()==0)
			throw new IndexOutOfBoundsException ();
		return get(size()-1);
	}

	/**
	 * Returns the first element of the first pair in the list.
	 *
	 * @param index ignored
	 * @return the first element of the first pair
	 */
	public T firstFirstElement(final int index)
	{
		return firstElement().first;
	}

	/**
	 * Returns the second element of the first pair in the list.
	 *
	 * @param index ignored
	 * @return the second element of the first pair
	 */
	public K firstSecondElement(final int index)
	{
		return firstElement().second;
	}

	/**
	 * Returns the first element of the last pair in the list.
	 *
	 * @param index ignored
	 * @return the first element of the last pair
	 */
	public T lastFirstElement(final int index)
	{
		return lastElement().first;
	}

	/**
	 * Returns the second element of the last pair in the list.
	 *
	 * @param index ignored
	 * @return the second element of the last pair
	 */
	public K lastSecondElement(final int index)
	{
		return lastElement().second;
	}

	/**
	 * Returns an array containing all the first elements in the list. If the
	 * given array is big enough, it is used, otherwise a new array of the same
	 * type is created.
	 *
	 * @param objs the array to use, if big enough
	 * @return the array of first elements
	 */
	@Override
	public T[] toArrayFirst(T[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getFirst(x);
		return objs;
	}

	/**
	 * Returns an array containing all the second elements in the list. If the
	 * given array is big enough, it is used, otherwise a new array of the same
	 * type is created.
	 *
	 * @param objs the array to use, if big enough
	 * @return the array of second elements
	 */
	@Override
	public K[] toArraySecond(K[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getSecond(x);
		return objs;
	}
}
