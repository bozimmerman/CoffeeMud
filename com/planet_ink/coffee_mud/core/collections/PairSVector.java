package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;

public class PairSVector<T, K> extends SVector<Pair<T, K>> implements List<Pair<T, K>>, PairList<T, K>
{
	private static final long	serialVersionUID	= -9175373358892311411L;

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
	public synchronized int indexOfFirst(T t)
	{
		return indexOfFirst(t, 0);
	}

	@Override
	public synchronized int indexOfSecond(K k)
	{
		return indexOfSecond(k, 0);
	}

	@Override
	public T getFirst(int index)
	{
		return get(index).first;
	}

	@Override
	public K getSecond(int index)
	{
		return get(index).second;
	}

	@Override
	public void add(T t, K k)
	{
		add(new Pair<T, K>(t, k));
	}

	public void addElement(T t, K k)
	{
		add(new Pair<T, K>(t, k));
	}

	@Override
	public boolean containsFirst(T t)
	{
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			if ((t == null) ? i.next() == null : t.equals(i.next().first))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsSecond(K k)
	{
		for (final Iterator<Pair<T, K>> i = iterator(); i.hasNext();)
		{
			if ((k == null) ? i.next() == null : k.equals(i.next().second))
				return true;
		}
		return false;
	}

	@Override
	public T elementAtFirst(int index)
	{
		return get(index).first;
	}

	@Override
	public K elementAtSecond(int index)
	{
		return get(index).second;
	}

	@Override
	public synchronized int indexOfFirst(T t, int index)
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
	public synchronized int indexOfSecond(K k, int index)
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
	public synchronized int lastIndexOfFirst(T t, int index)
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
	public synchronized int lastIndexOfSecond(K k, int index)
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
	public synchronized int lastIndexOfFirst(T t)
	{
		return lastIndexOfFirst(t, size() - 1);
	}

	@Override
	public synchronized int lastIndexOfSecond(K k)
	{
		return lastIndexOfSecond(k, size() - 1);
	}

	@Override
	public boolean removeFirst(T t)
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
	public boolean removeSecond(K k)
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
	public boolean removeElementFirst(T t)
	{
		return removeFirst(t);
	}

	@Override
	public boolean removeElementSecond(K k)
	{
		return removeSecond(k);
	}

	public T firstFirstElement(int index)
	{
		return firstElement().first;
	}

	public K firstSecondElement(int index)
	{
		return firstElement().second;
	}

	public T lastFirstElement(int index)
	{
		return lastElement().first;
	}

	public K lastSecondElement(int index)
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
