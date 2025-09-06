package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;

/*
   Copyright 2012-2025 Bo Zimmerman

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
 * A simple linked list implementation of a PairList, based on CMList.
 * @param <T> the type of the first object
 * @param <K> the type of the second object
 */
public final class PairCMList<T, K> extends CMList<Pair<T, K>> implements PairList<T, K>
{
	private static final long	serialVersionUID	= -9175373328892311411L;

	/**
	 * Gets a converter that converts a Pair to its first value.
	 * @return the converter
	 */
	@Override
	public final Pair.FirstConverter<T, K> getFirstConverter()
	{
		return new Pair.FirstConverter<T, K>();
	}

	/**
	 * Gets a converter that converts a Pair to its second value.
	 *
	 * @return the converter
	 */
	@Override
	public final Pair.SecondConverter<T, K> getSecondConverter()
	{
		return new Pair.SecondConverter<T, K>();
	}

	/**
	 * Gets a masking library iterator that converts a Pair to its first value.
	 *
	 * @return the iterator
	 */
	@Override
	public final Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Pair<T, K>, T>(iterator(), getFirstConverter());
	}

	/**
	 * Gets a masking library iterator that converts a Pair to its second value.
	 *
	 * @return the iterator
	 */
	@Override
	public final Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Pair<T, K>, K>(iterator(), getSecondConverter());
	}

	/**
	 * Returns the index of the first occurrence of the specified element in this list,
	 * or -1 if this list does not contain the first element.
	 *
	 * @param t the first element to search for
	 * @return the index of the first occurrence of the specified element in this list,
	 */
	@Override
	public synchronized int indexOfFirst(final T t)
	{
		return indexOfFirst(t, 0);
	}

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this list, or -1 if this list does not contain the second element.
	 *
	 * @param k the second element to search for
	 * @return the index of the first occurrence of the specified element in
	 *         this list,
	 */
	@Override
	public synchronized int indexOfSecond(final K k)
	{
		return indexOfSecond(k, 0);
	}

	/**
	 * Returns the first element of the pair at the specified position in this list.
	 *
	 * @param index index of the pair to return
	 * @return the first element of the pair at the specified position in this list.
	 */
	@Override
	public T getFirst(final int index)
	{
		return get(index).first;
	}

	/**
	 * Returns the second element of the pair at the specified position in this
	 * list.
	 *
	 * @param index index of the pair to return
	 * @return the second element of the pair at the specified position in this
	 *         list.
	 */
	@Override
	public K getSecond(final int index)
	{
		return get(index).second;
	}

	/**
	 * Adds a pair to the end of the list.
	 *
	 * @param t the first element of the pair to add
	 * @param k the second element of the pair to add
	 */
	@Override
	public void add(final T t, final K k)
	{
		add(new Pair<T, K>(t, k));
	}

	/**
	 * Adds a pair to the specified position in the list.
	 *
	 * @param x the position in the list to add the pair
	 * @param t the first element of the pair to add
	 * @param k the second element of the pair to add
	 */
	@Override
	public void add(final int x, final T t, final K k)
	{
		add(x, new Pair<T, K>(t, k));
	}

	/**
	 * Adds a pair to the end of the list.
	 *
	 * @param t the first element of the pair to add
	 * @param k the second element of the pair to add
	 */
	public void addElement(final T t, final K k)
	{
		add(new Pair<T, K>(t, k));
	}

	/**
	 * Adds a pair to the specified position in the list.
	 *
	 * @param x the position in the list to add the pair
	 * @param t the first element of the pair to add
	 * @param k the second element of the pair to add
	 */
	public void addElement(final int x, final T t, final K k)
	{
		add(x, new Pair<T, K>(t, k));
	}

	/**
	 * Returns true if this list contains at least one pair with the
	 * given first element.
	 *
	 * @param t the first element to search for
	 * @return true if this list contains at least one pair with the
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
	 * Returns true if this list contains at least one pair with the given
	 * second element.
	 *
	 * @param k the second element to search for
	 * @return true if this list contains at least one pair with the given
	 *         second element.
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
	 * Returns the first element of the pair at the specified position in this list.
	 *
	 * @param index index of the pair to return
	 * @return the first element of the pair at the specified position in this list.
	 */
	@Override
	public T elementAtFirst(final int index)
	{
		return get(index).first;
	}

	/**
	 * Returns the second element of the pair at the specified position in this
	 * list.
	 *
	 * @param index index of the pair to return
	 * @return the second element of the pair at the specified position in this
	 *         list.
	 */
	@Override
	public K elementAtSecond(final int index)
	{
		return get(index).second;
	}

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this list, starting at the specified index, or -1 if this list does not
	 * contain the first element.
	 *
	 * @param t the first element to search for
	 * @param index the index to start searching from
	 * @return the index of the first occurrence of the specified element in
	 *         this list,
	 */
	@Override
	public synchronized int indexOfFirst(final T t, final int index)
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
	 * Returns the index of the first occurrence of the specified element in
	 * this list, starting at the specified index, or -1 if this list does not
	 * contain the second element.
	 *
	 * @param k the second element to search for
	 * @param index the index to start searching from
	 * @return the index of the first occurrence of the specified element in
	 *         this list,
	 */
	@Override
	public synchronized int indexOfSecond(final K k, final int index)
	{
		try
		{
			for (int i = index; i < size(); i++)
				if ((k == null ? get(i).second == null : k.equals(get(i).second)))
					return i;
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this
	 * list, searching backwards from the specified index, or -1 if this list
	 * does not contain the first element.
	 *
	 * @param t the first element to search for
	 * @param index the index to start searching backwards from
	 * @return the index of the last occurrence of the specified element in this
	 *         list,
	 */
	@Override
	public synchronized int lastIndexOfFirst(final T t, final int index)
	{
		try
		{
			for (int i = index; i >= 0; i--)
				if ((t == null ? get(i).first == null : t.equals(get(i).first)))
					return i;
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this
	 * list, searching backwards from the specified index, or -1 if this list
	 * does not contain the second element.
	 *
	 * @param k the second element to search for
	 * @param index the index to start searching backwards from
	 * @return the index of the last occurrence of the specified element in this
	 *         list,
	 */
	@Override
	public synchronized int lastIndexOfSecond(final K k, final int index)
	{
		try
		{
			for (int i = index; i >= 0; i--)
				if ((k == null ? get(i).second == null : k.equals(get(i).second)))
					return i;
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this
	 * list, or -1 if this list does not contain the first element.
	 *
	 * @param t the first element to search for
	 * @return the index of the last occurrence of the specified element in this
	 *         list,
	 */
	@Override
	public synchronized int lastIndexOfFirst(final T t)
	{
		return lastIndexOfFirst(t, size() - 1);
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this
	 * list, or -1 if this list does not contain the second element.
	 *
	 * @param k the second element to search for
	 * @return the index of the last occurrence of the specified element in this
	 *         list,
	 */
	@Override
	public synchronized int lastIndexOfSecond(final K k)
	{
		return lastIndexOfSecond(k, size() - 1);
	}

	/**
	 * Removes the first occurrence of the specified element in this list, if it
	 * is present. If the list does not contain the first element, it is
	 * unchanged. More formally, removes the first element e such that
	 * (t==null ? e.first==null : t.equals(e.first)). Returns true if this
	 * list contained the specified element (or equivalently, if this list
	 * changed as a result of the call).
	 *
	 * @param t the first element to search for
	 * @return true if this list contained the specified element
	 */
	@Override
	public boolean removeFirst(final T t)
	{
		Pair<T, K> pair;
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			pair = i.next();
			if ((t == null ? pair.first == null : t.equals(pair.first)))
				return super.remove(pair);
		}
		return false;
	}

	/**
	 * Removes the first occurrence of the specified element in this list, if it
	 * is present. If the list does not contain the second element, it is
	 * unchanged. More formally, removes the first element e such that (k==null
	 * ? e.second==null : k.equals(e.second)). Returns true if this list
	 * contained the specified element (or equivalently, if this list changed as
	 * a result of the call).
	 *
	 * @param k the second element to search for
	 * @return true if this list contained the specified element
	 */
	@Override
	public boolean removeSecond(final K k)
	{
		Pair<T, K> pair;
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			pair = i.next();
			if ((k == null ? pair.second == null : k.equals(pair.second)))
				return super.remove(pair);
		}
		return false;
	}

	/**
	 * Removes the first occurrence of the specified element in this list, if it
	 * is present. If the list does not contain the first element, it is
	 * unchanged. More formally, removes the first element e such that (t==null
	 * ? e.first==null : t.equals(e.first)). Returns true if this list contained
	 * the specified element (or equivalently, if this list changed as a result
	 * of the call).
	 *
	 * @param t the first element to search for
	 * @return true if this list contained the specified element
	 */
	@Override
	public boolean removeElementFirst(final T t)
	{
		return removeFirst(t);
	}

	/**
	 * Removes the first occurrence of the specified element in this list, if it
	 * is present. If the list does not contain the second element, it is
	 * unchanged. More formally, removes the first element e such that (k==null
	 * ? e.second==null : k.equals(e.second)). Returns true if this list
	 * contained the specified element (or equivalently, if this list changed as
	 * a result of the call).
	 *
	 * @param k the second element to search for
	 * @return true if this list contained the specified element
	 */
	@Override
	public boolean removeElementSecond(final K k)
	{
		return removeSecond(k);
	}

	/**
	 * Returns an array containing all of the first elements in this list in
	 * proper sequence (from first to last element).
	 *
	 * @param objs the array to use, if it is big enough
	 * @return an array containing all of the first elements in this list in
	 *         proper sequence
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
	 * Returns an array containing all of the second elements in this list in
	 * proper sequence (from first to last element).
	 *
	 * @param objs the array to use, if it is big enough
	 * @return an array containing all of the second elements in this list in
	 *         proper sequence
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
