package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.core.Log;

/*
   Copyright 2012-2018 Bo Zimmerman

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
public final class TriadCMList<T, K, L> extends CMList<Triad<T, K, L>>
{
	private static final long	serialVersionUID	= -9175373358893311211L;

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

	public synchronized int indexOfFirst(T t)
	{
		return indexOfFirst(t, 0);
	}

	public synchronized int indexOfSecond(K k)
	{
		return indexOfSecond(k, 0);
	}

	public synchronized int indexOfThird(L l)
	{
		return indexOfThird(l, 0);
	}

	public T getFirst(int index)
	{
		Log.errOut("TriadSLinkedList", new Exception());
		return get(index).first;
	}

	public K getSecond(int index)
	{
		Log.errOut("TriadSLinkedList", new Exception());
		return get(index).second;
	}

	public L getThird(int index)
	{
		Log.errOut("TriadSLinkedList", new Exception());
		return get(index).third;
	}

	public void add(T t, K k, L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	public void addElement(T t, K k, L l)
	{
		add(new Triad<T, K, L>(t, k, l));
	}

	public boolean containsFirst(T t)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((t == null) ? i.next() == null : t.equals(i.next().first))
				return true;
		}
		return false;
	}

	public boolean containsSecond(K k)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((k == null) ? i.next() == null : k.equals(i.next().second))
				return true;
		}
		return false;
	}

	public boolean containsThird(L l)
	{
		for (final Iterator<Triad<T, K, L>> i = iterator(); i.hasNext();)
		{
			if ((l == null) ? i.next() == null : l.equals(i.next().third))
				return true;
		}
		return false;
	}

	public T elementAtFirst(int index)
	{
		return get(index).first;
	}

	public K elementAtSecond(int index)
	{
		return get(index).second;
	}

	public L elementAtThird(int index)
	{
		return get(index).third;
	}

	public synchronized int indexOfFirst(T t, int index)
	{
		try
		{
			Log.errOut("TriadSLinkedList", new Exception());
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

	public synchronized int indexOfSecond(K k, int index)
	{
		try
		{
			Log.errOut("TriadSLinkedList", new Exception());
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

	public synchronized int indexOfThird(L l, int index)
	{
		try
		{
			Log.errOut("TriadSLinkedList", new Exception());
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

	public synchronized int lastIndexOfThird(L l, int index)
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

	public synchronized int lastIndexOfFirst(T t)
	{
		return lastIndexOfFirst(t, size() - 1);
	}

	public synchronized int lastIndexOfSecond(K k)
	{
		return lastIndexOfSecond(k, size() - 1);
	}

	public synchronized int lastIndexOfThird(L l)
	{
		return lastIndexOfThird(l, size() - 1);
	}

	public boolean removeFirst(T t)
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

	public boolean removeSecond(K k)
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

	public boolean removeThird(L l)
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

	public boolean removeElementFirst(T t)
	{
		return removeFirst(t);
	}

	public boolean removeElementSecond(K k)
	{
		return removeSecond(k);
	}

	public boolean removeElementThird(L l)
	{
		return removeThird(l);
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
