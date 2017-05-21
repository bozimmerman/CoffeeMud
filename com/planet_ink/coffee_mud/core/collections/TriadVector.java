package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;

public class TriadVector<T, K, L> extends Vector<Triad<T, K, L>> implements TriadList<T, K, L>
{
	private static final long	serialVersionUID	= -9175373358892311411L;

	public TriadVector()
	{
		super();
	}
	
	public TriadVector(TriadList<T, K, L> list)
	{
		super();
		if(list != null)
		{
			for(Triad<T, K, L> t : list)
				add(t.first, t.second, t.third);
		}
	}
	
	@Override
	public Triad.FirstConverter<T, K, L> getFirstConverter()
	{
		return new Triad.FirstConverter<T, K, L>();
	}

	@Override
	public Triad.SecondConverter<T, K, L> getSecondConverter()
	{
		return new Triad.SecondConverter<T, K, L>();
	}

	@Override
	public Triad.ThirdConverter<T, K, L> getThirdConverter()
	{
		return new Triad.ThirdConverter<T, K, L>();
	}

	public Enumeration<T> firstElements()
	{
		return new ConvertingEnumeration<Triad<T, K, L>, T>(elements(), getFirstConverter());
	}

	public Enumeration<K> secondElements()
	{
		return new ConvertingEnumeration<Triad<T, K, L>, K>(elements(), getSecondConverter());
	}

	public Enumeration<L> thirdElements()
	{
		return new ConvertingEnumeration<Triad<T, K, L>, L>(elements(), getThirdConverter());
	}

	@Override
	public Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Triad<T, K, L>, T>(iterator(), getFirstConverter());
	}

	@Override
	public Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Triad<T, K, L>, K>(iterator(), getSecondConverter());
	}

	@Override
	public Iterator<L> thirdIterator()
	{
		return new ConvertingIterator<Triad<T, K, L>, L>(iterator(), getThirdConverter());
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
	public synchronized int indexOfThird(L l)
	{
		return indexOfThird(l, 0);
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
	public L getThird(int index)
	{
		return get(index).third;
	}

	@Override
	public void add(T t, K k, L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	public void addElement(T t, K k, L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	@Override
	public boolean containsFirst(T t)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((t == null) ? i.next() == null : t.equals(i.next().first))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsSecond(K k)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((k == null) ? i.next() == null : k.equals(i.next().second))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsThird(L l)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((l == null) ? i.next() == null : l.equals(i.next().third))
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
	public L elementAtThird(int index)
	{
		return get(index).third;
	}

	@SuppressWarnings("unchecked")

	@Override
	public boolean contains(Object o)
	{
		if (o instanceof Triad)
			return super.contains(o);
		if (containsFirst((T) o))
			return true;
		return containsSecond((K) o);
	}

	@SuppressWarnings("unchecked")

	@Override
	public int indexOf(Object o)
	{
		if (o instanceof Triad)
			return super.indexOf(o);
		final int x = indexOfFirst((T) o);
		if (x >= 0)
			return x;
		return indexOfSecond((K) o);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized int indexOf(Object o, int index)
	{
		if (o instanceof Triad)
			return super.indexOf(o, index);
		final int x = indexOfFirst((T) o, index);
		if (x >= 0)
			return x;
		return indexOfSecond((K) o, index);
	}
	
	@Override
	public int indexOfFirst(T t, int index)
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
	public int indexOfSecond(K k, int index)
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
	public int indexOfThird(L l, int index)
	{
		try
		{
			for (int i = index; i < size(); i++)
			{
				if ((l == null ? get(i).third == null : l.equals(get(i).third)))
					return i;
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	@Override
	public int lastIndexOfFirst(T t, int index)
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
	public int lastIndexOfSecond(K k, int index)
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
	public int lastIndexOfThird(L l, int index)
	{
		try
		{
			for (int i = index; i >= 0; i--)
			{
				if ((l == null ? get(i).third == null : l.equals(get(i).third)))
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
	public synchronized int lastIndexOfThird(L l)
	{
		return lastIndexOfThird(l, size() - 1);
	}

	@Override
	public synchronized boolean removeFirst(T t)
	{
		Triad<T, K, L> pair;
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
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
	public synchronized boolean removeSecond(K k)
	{
		Triad<T, K, L> pair;
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
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
	public synchronized boolean removeThird(L l)
	{
		Triad<T, K, L> pair;
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			pair = i.next();
			if ((l == null ? pair.third == null : l.equals(pair.third)))
			{
				i.remove();
				return true;
			}
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

	@Override
	public boolean removeElementThird(L l)
	{
		return removeThird(l);
	}

	public T firstFirstElement(int index)
	{
		return firstElement().first;
	}

	public K firstSecondElement(int index)
	{
		return firstElement().second;
	}

	public L firstThirdElement(int index)
	{
		return firstElement().third;
	}

	public T lastFirstElement(int index)
	{
		return lastElement().first;
	}

	public K lastSecondElement(int index)
	{
		return lastElement().second;
	}

	public L lastThirdElement(int index)
	{
		return lastElement().third;
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

	@Override
	public L[] toArrayThird(L[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getThird(x);
		return objs;
	}
}
