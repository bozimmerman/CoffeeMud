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
public class QuadVector<T, K, L, M> extends Vector<Quad<T, K, L, M>> implements List<Quad<T, K, L, M>>
{
	private static final long	serialVersionUID	= -9175373358892311411L;

	public QuadVector()
	{
		super();
	}

	public QuadVector(final int initial)
	{
		super(initial);
	}

	public QuadVector(final QuadVector<T, K, L, M> list)
	{
		super();
		if(list != null)
		{
			for(final Quad<T, K, L, M> t : list)
				add(t.first, t.second, t.third, t.fourth);
		}
	}

	public Quad.FirstConverter<T, K, L, M> getFirstConverter()
	{
		return new Quad.FirstConverter<T, K, L, M>();
	}

	public Quad.SecondConverter<T, K, L, M> getSecondConverter()
	{
		return new Quad.SecondConverter<T, K, L, M>();
	}

	public Quad.ThirdConverter<T, K, L, M> getThirdConverter()
	{
		return new Quad.ThirdConverter<T, K, L, M>();
	}

	public Quad.FourthConverter<T, K, L, M> getFourthConverter()
	{
		return new Quad.FourthConverter<T, K, L, M>();
	}

	public Enumeration<T> firstElements()
	{
		return new ConvertingEnumeration<Quad<T, K, L, M>, T>(elements(), getFirstConverter());
	}

	public Enumeration<K> secondElements()
	{
		return new ConvertingEnumeration<Quad<T, K, L, M>, K>(elements(), getSecondConverter());
	}

	public Enumeration<L> thirdElements()
	{
		return new ConvertingEnumeration<Quad<T, K, L, M>, L>(elements(), getThirdConverter());
	}

	public Enumeration<M> fourthElements()
	{
		return new ConvertingEnumeration<Quad<T, K, L, M>, M>(elements(), getFourthConverter());
	}

	public Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Quad<T, K, L, M>, T>(iterator(), getFirstConverter());
	}

	public Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Quad<T, K, L, M>, K>(iterator(), getSecondConverter());
	}

	public Iterator<L> thirdIterator()
	{
		return new ConvertingIterator<Quad<T, K, L, M>, L>(iterator(), getThirdConverter());
	}

	public Iterator<M> fourthIterator()
	{
		return new ConvertingIterator<Quad<T, K, L, M>, M>(iterator(), getFourthConverter());
	}

	@SuppressWarnings("unchecked")

	@Override
	public boolean contains(final Object o)
	{
		if (o instanceof Quad)
			return super.contains(o);
		if (containsFirst((T) o))
			return true;
		return containsSecond((K) o);
	}

	@SuppressWarnings("unchecked")

	@Override
	public int indexOf(final Object o)
	{
		if (o instanceof Quad)
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
		if (o instanceof Quad)
			return super.indexOf(o, index);
		final int x = indexOfFirst((T) o, index);
		if (x >= 0)
			return x;
		return indexOfSecond((K) o, index);
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

	public synchronized int indexOfFourth(final M m)
	{
		return indexOfFourth(m, 0);
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

	public M getFourth(final int index)
	{
		return get(index).fourth;
	}

	public void add(final T t, final K k, final L l, final M m)
	{
		add(new Quad<T, K, L, M>(t, k, l, m));
	}

	public void addElement(final T t, final K k, final L l, final M m)
	{
		add(new Quad<T, K, L, M>(t, k, l, m));
	}

	public boolean containsFirst(final T t)
	{
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
		{
			if ((t == null) ? i.next() == null : t.equals(i.next().first))
				return true;
		}
		return false;
	}

	public boolean containsSecond(final K k)
	{
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
		{
			if ((k == null) ? i.next() == null : k.equals(i.next().second))
				return true;
		}
		return false;
	}

	public boolean containsThird(final L l)
	{
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
		{
			if ((l == null) ? i.next() == null : l.equals(i.next().third))
				return true;
		}
		return false;
	}

	public boolean containsFourth(final M m)
	{
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
		{
			if ((m == null) ? i.next() == null : m.equals(i.next().fourth))
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

	public M elementAtFourth(final int index)
	{
		return get(index).fourth;
	}

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

	public int indexOfFourth(final M m, final int index)
	{
		try
		{
			for (int i = index; i < size(); i++)
			{
				if ((m == null ? get(i).fourth == null : m.equals(get(i).fourth)))
					return i;
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

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

	public int lastIndexOfFourth(final M m, final int index)
	{
		try
		{
			for (int i = index; i >= 0; i--)
			{
				if ((m == null ? get(i).fourth == null : m.equals(get(i).fourth)))
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

	public synchronized int lastIndexOfFourth(final M m)
	{
		return lastIndexOfFourth(m, size() - 1);
	}

	public synchronized boolean removeFirst(final T t)
	{
		Quad<T, K, L, M> pair;
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
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

	public synchronized boolean removeSecond(final K k)
	{
		Quad<T, K, L, M> pair;
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
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

	public synchronized boolean removeThird(final L l)
	{
		Quad<T, K, L, M> pair;
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
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

	public synchronized boolean removeFourth(final M m)
	{
		Quad<T, K, L, M> pair;
		for (final Iterator<Quad<T, K, L, M>> i = iterator(); i.hasNext();)
		{
			pair = i.next();
			if ((m == null ? pair.fourth == null : m.equals(pair.fourth)))
			{
				i.remove();
				return true;
			}
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

	public boolean removeElementFourth(final M m)
	{
		return removeFourth(m);
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

	public M firstFourthElement(final int index)
	{
		return firstElement().fourth;
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

	public M lastFourthElement(final int index)
	{
		return lastElement().fourth;
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

	public M[] toArrayFourth(M[] objs)
	{
		if(objs.length < size())
			objs = Arrays.copyOf(objs, size());
		for (int x = 0; x < size(); x++)
			objs[x] = getFourth(x);
		return objs;
	}
}
