package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;

/*
   Copyright 2010-2020 Bo Zimmerman

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
public final class TriadSVector<T, K, L> extends SVector<Triad<T, K, L>>
{
	private static final long	serialVersionUID	= -9175373358893311411L;

	public final Triad.FirstConverter<T, K, L> getFirstConverter()
	{
		return new Triad.FirstConverter<T, K, L>();
	}

	public final Triad.SecondConverter<T, K, L> getSecondConverter()
	{
		return new Triad.SecondConverter<T, K, L>();
	}

	public final Triad.ThirdConverter<T, K, L> getThirdConverter()
	{
		return new Triad.ThirdConverter<T, K, L>();
	}

	public final Enumeration<T> firstElements()
	{
		return new ConvertingEnumeration<Triad<T, K, L>, T>(elements(), getFirstConverter());
	}

	public final Enumeration<K> secondElements()
	{
		return new ConvertingEnumeration<Triad<T, K, L>, K>(elements(), getSecondConverter());
	}

	public final Enumeration<L> thirdElements()
	{
		return new ConvertingEnumeration<Triad<T, K, L>, L>(elements(), getThirdConverter());
	}

	public final Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Triad<T, K, L>, T>(iterator(), getFirstConverter());
	}

	public final Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Triad<T, K, L>, K>(iterator(), getSecondConverter());
	}

	public final Iterator<L> thirdIterator()
	{
		return new ConvertingIterator<Triad<T, K, L>, L>(iterator(), getThirdConverter());
	}

	public synchronized int indexOfFirst(final T t)
	{
		return indexOfFirst(t, 0);
	}

	public synchronized int indexOfSecond(final K k)
	{
		return indexOfSecond(k, 0);
	}

	public synchronized int indexOfThird(final L l)
	{
		return indexOfThird(l, 0);
	}

	public T getFirst(final int index)
	{
		return get(index).first;
	}

	public K getSecond(final int index)
	{
		return get(index).second;
	}

	public L getThird(final int index)
	{
		return get(index).third;
	}

	public void add(final T t, final K k, final L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	public void addElement(final T t, final K k, final L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	public boolean containsFirst(final T t)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((t == null) ? i.next() == null : t.equals(i.next().first))
				return true;
		}
		return false;
	}

	public boolean containsSecond(final K k)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((k == null) ? i.next() == null : k.equals(i.next().second))
				return true;
		}
		return false;
	}

	public boolean containsThird(final L l)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((l == null) ? i.next() == null : l.equals(i.next().third))
				return true;
		}
		return false;
	}

	public T elementAtFirst(final int index)
	{
		return get(index).first;
	}

	public K elementAtSecond(final int index)
	{
		return get(index).second;
	}

	public L elementAtThird(final int index)
	{
		return get(index).third;
	}

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

	public synchronized int indexOfThird(final L l, final int index)
	{
		try
		{
			for (int i = 0; i < size(); i++)
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

	public synchronized int lastIndexOfThird(final L l, final int index)
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

	public synchronized int lastIndexOfFirst(final T t)
	{
		return lastIndexOfFirst(t, size() - 1);
	}

	public synchronized int lastIndexOfSecond(final K k)
	{
		return lastIndexOfSecond(k, size() - 1);
	}

	public synchronized int lastIndexOfThird(final L l)
	{
		return lastIndexOfThird(l, size() - 1);
	}

	public boolean removeFirst(final T t)
	{
		Triad<T, K, L> triad;
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			triad = i.next();
			if ((t == null ? triad.first == null : t.equals(triad.first)))
				return super.remove(triad);
		}
		return false;
	}

	public boolean removeSecond(final K k)
	{
		Triad<T, K, L> triad;
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			triad = i.next();
			if ((k == null ? triad.second == null : k.equals(triad.second)))
				return super.remove(triad);
		}
		return false;
	}

	public boolean removeThird(final L l)
	{
		Triad<T, K, L> triad;
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			triad = i.next();
			if ((l == null ? triad.third == null : l.equals(triad.third)))
				return super.remove(triad);
		}
		return false;
	}

	public boolean removeElementFirst(final T t)
	{
		return removeFirst(t);
	}

	public boolean removeElementSecond(final K k)
	{
		return removeSecond(k);
	}

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

	public T[] toArrayFirst(T[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getFirst(x);
		return objs;
	}

	public K[] toArraySecond(K[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getSecond(x);
		return objs;
	}

	public L[] toArrayThird(L[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getThird(x);
		return objs;
	}
}
