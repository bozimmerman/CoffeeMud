package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;

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
public class TriadVector<T, K, L> extends Vector<Triad<T, K, L>> implements TriadList<T, K, L>
{
	private static final long	serialVersionUID	= -9175373358892311411L;

	public TriadVector()
	{
		super();
	}

	public TriadVector(final int initial)
	{
		super(initial);
	}

	public TriadVector(final TriadList<T, K, L> list)
	{
		super();
		if(list != null)
		{
			for(final Triad<T, K, L> t : list)
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
	public synchronized int indexOfThird(final L l)
	{
		return indexOfThird(l, 0);
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
	public L getThird(final int index)
	{
		return get(index).third;
	}

	@Override
	public void add(final T t, final K k, final L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	public void addElement(final T t, final K k, final L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	@Override
	public boolean containsFirst(final T t)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((t == null) ? i.next() == null : t.equals(i.next().first))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsSecond(final K k)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((k == null) ? i.next() == null : k.equals(i.next().second))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsThird(final L l)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((l == null) ? i.next() == null : l.equals(i.next().third))
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
	public L elementAtThird(final int index)
	{
		return get(index).third;
	}

	@SuppressWarnings("unchecked")

	@Override
	public boolean contains(final Object o)
	{
		if (o instanceof Triad)
			return super.contains(o);
		if (containsFirst((T) o))
			return true;
		return containsSecond((K) o);
	}

	@SuppressWarnings("unchecked")

	@Override
	public int indexOf(final Object o)
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
	public synchronized int indexOf(final Object o, final int index)
	{
		if (o instanceof Triad)
			return super.indexOf(o, index);
		final int x = indexOfFirst((T) o, index);
		if (x >= 0)
			return x;
		return indexOfSecond((K) o, index);
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
	public int indexOfThird(final L l, final int index)
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
	public int lastIndexOfThird(final L l, final int index)
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
	public synchronized int lastIndexOfThird(final L l)
	{
		return lastIndexOfThird(l, size() - 1);
	}

	@Override
	public synchronized boolean removeFirst(final T t)
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
	public synchronized boolean removeSecond(final K k)
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
	public synchronized boolean removeThird(final L l)
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
	public boolean removeElementThird(final L l)
	{
		return removeThird(l);
	}

	public T firstFirstElement(final int index)
	{
		return firstElement().first;
	}

	public K firstSecondElement(final int index)
	{
		return firstElement().second;
	}

	public L firstThirdElement(final int index)
	{
		return firstElement().third;
	}

	public T lastFirstElement(final int index)
	{
		return lastElement().first;
	}

	public K lastSecondElement(final int index)
	{
		return lastElement().second;
	}

	public L lastThirdElement(final int index)
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
