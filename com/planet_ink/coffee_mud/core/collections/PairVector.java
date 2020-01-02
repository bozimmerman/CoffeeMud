package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2012-2020 Bo Zimmerman

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
public class PairVector<T, K> extends Vector<Pair<T, K>> implements PairList<T, K>
{
	/**
	 *
	 */
	private static final long	serialVersionUID	= 1672867955945287259L;

	public PairVector(final List<Pair<T,K>> initial)
	{
		super(initial.size());
		addAll(initial);
	}

	public PairVector()
	{
		super();
	}

	public PairVector(final int x)
	{
		super(x);
	}

	@Override
	public Pair.FirstConverter<T, K> getFirstConverter()
	{
		return new Pair.FirstConverter<T, K>();
	}

	@Override
	public Pair.SecondConverter<T, K> getSecondConverter()
	{
		return new Pair.SecondConverter<T, K>();
	}

	public Enumeration<T> firstElements()
	{
		return new ConvertingEnumeration<Pair<T, K>, T>(elements(), getFirstConverter());
	}

	public Enumeration<K> secondElements()
	{
		return new ConvertingEnumeration<Pair<T, K>, K>(elements(), getSecondConverter());
	}

	@Override
	public Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Pair<T, K>, T>(iterator(), getFirstConverter());
	}

	@Override
	public Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Pair<T, K>, K>(iterator(), getSecondConverter());
	}

	@Override
	public synchronized int indexOfFirst(final T t)
	{
		return indexOfFirst(t, 0);
	}

	@Override
	public synchronized int indexOfSecond(final K k)
	{
		return indexOfSecond(k, 0);
	}

	@Override
	public T getFirst(final int index)
	{
		return get(index).first;
	}

	@Override
	public K getSecond(final int index)
	{
		return get(index).second;
	}

	@Override
	public void add(final T t, final K k)
	{
		add(new Pair<T, K>(t, k));
	}

	public void addElement(final T t, final K k)
	{
		add(new Pair<T, K>(t, k));
	}

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

	@SuppressWarnings("unchecked")

	@Override
	public synchronized int indexOf(final Object o, final int index)
	{
		if (o instanceof Pair)
			return super.indexOf(o, index);
		final int x = indexOfFirst((T) o, index);
		if (x >= 0)
			return x;
		return indexOfSecond((K) o, index);
	}

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

	@Override
	public T elementAtFirst(final int index)
	{
		return get(index).first;
	}

	@Override
	public K elementAtSecond(final int index)
	{
		return get(index).second;
	}

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

	@Override
	public int lastIndexOfFirst(final T t)
	{
		return lastIndexOfFirst(t, size() - 1);
	}

	@Override
	public int lastIndexOfSecond(final K k)
	{
		return lastIndexOfSecond(k, size() - 1);
	}

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

	@Override
	public boolean removeElementFirst(final T t)
	{
		return removeFirst(t);
	}

	@Override
	public boolean removeElementSecond(final K k)
	{
		return removeSecond(k);
	}

	public T firstFirstElement(final int index)
	{
		return firstElement().first;
	}

	public K firstSecondElement(final int index)
	{
		return firstElement().second;
	}

	public T lastFirstElement(final int index)
	{
		return lastElement().first;
	}

	public K lastSecondElement(final int index)
	{
		return lastElement().second;
	}

	@Override
	public T[] toArrayFirst(T[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getFirst(x);
		return objs;
	}

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
