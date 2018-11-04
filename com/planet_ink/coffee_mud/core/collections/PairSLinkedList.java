package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;

public final class PairSLinkedList<T, K> extends SLinkedList<Pair<T, K>> implements PairList<T, K>
{
	private static final long	serialVersionUID	= -9175373328892311411L;

	@Override
	public final Pair.FirstConverter<T, K> getFirstConverter()
	{
		return new Pair.FirstConverter<T, K>();
	}

	@Override
	public final Pair.SecondConverter<T, K> getSecondConverter()
	{
		return new Pair.SecondConverter<T, K>();
	}

	@Override
	public final Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Pair<T, K>, T>(iterator(), getFirstConverter());
	}

	@Override
	public final Iterator<K> secondIterator()
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

	@Override
	public synchronized int indexOfSecond(final K k, final int index)
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
	public synchronized int lastIndexOfFirst(final T t, final int index)
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
	public synchronized int lastIndexOfSecond(final K k, final int index)
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
	public synchronized int lastIndexOfFirst(final T t)
	{
		return lastIndexOfFirst(t, size() - 1);
	}

	@Override
	public synchronized int lastIndexOfSecond(final K k)
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
				return super.remove(pair);
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
				return super.remove(pair);
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
