package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.OperationNotSupportedException;

/*
   Copyright 2023-2023 Bo Zimmerman

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

/*
 * A List that does not return items after an ellapsed period of time after being added.
 */
public class ExpireVector<T> implements Serializable, Iterable<T>, Collection<T>, CList<T>, RandomAccess
{
	private static final long	serialVersionUID	= 6687178785122561992L;

	private CopyOnWriteArrayList<Pair<T,Long>> underList = new CopyOnWriteArrayList<Pair<T,Long>>();

	private List<Pair<T,Long>> list = Collections.synchronizedList(underList);

	private long expirationTime = 2 * 60000; // two minutes

	protected final Converter<Pair<T,Long>,T> fconv = new Converter<Pair<T,Long>,T>() {
		@Override
		public T convert(final Pair<T, Long> obj)
		{
			if ((obj != null)&& (System.currentTimeMillis()<obj.second.longValue()))
				return obj.first;
			return null;
		}
	};

	protected Long defTime()
	{
		return Long.valueOf(System.currentTimeMillis() + expirationTime);
	}

	public ExpireVector()
	{
		super();
	}

	public ExpireVector(final long defaultExpirationTime)
	{
		super();
		this.expirationTime = defaultExpirationTime;
	}

	public void addAll(final Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				add(T.nextElement());
		}
	}

	public void addAll(final T[] T)
	{
		if (T != null)
		{
			for (final T e : T)
				add(e);
		}
	}

	public void addAll(final Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				add(T.next());
		}
	}

	public void removeAll(final Enumeration<T> T)
	{
		if (T != null)
		{
			for (; T.hasMoreElements();)
				remove(T.nextElement());
		}
	}

	public void removeAll(final Iterator<T> T)
	{
		if (T != null)
		{
			for (; T.hasNext();)
				remove(T.next());
		}
	}

	public void removeAll(final List<T> T)
	{
		if (T != null)
		{
			for (final T o : T)
				remove(o);
		}
	}

	public int capacity()
	{
		return size();
	}

	@SuppressWarnings("unchecked")
	public ExpireVector<T> copyOf()
	{
		try
		{
			synchronized(this)
			{
				final ExpireVector<T> copy = new ExpireVector<T>();
				copy.underList = (CopyOnWriteArrayList<Pair<T,Long>>)underList.clone();
				copy.list = Collections.synchronizedList(copy.underList);
				return copy;
			}
		}
		catch (final Exception e)
		{
			return new ExpireVector<T>();
		}
	}

	@Override
	public ExpireVector<T> clone()
	{
		return copyOf();
	}

	public void copyInto(final Object[] anArray)
	{
		toArray(anArray);
	}

	public T elementAt(final int index)
	{
		return get(index);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<T> elements()
	{
		if(list.size()==0)
			return EmptyEnumeration.INSTANCE;
		return new Enumeration<T>()
		{
			final Iterator<T>	i	= iterator();

			@Override
			public boolean hasMoreElements()
			{
				return i.hasNext();
			}

			@Override
			public T nextElement()
			{
				return i.next();
			}
		};
	}

	public void ensureCapacity(final int minCapacity)
	{
		throw new IllegalArgumentException();
	}

	public T firstElement()
	{
		return (size() == 0) ? null : get(0);
	}

	public T lastElement()
	{
		return (size() == 0) ? null : get(size() - 1);
	}

	public void setElementAt(final T obj, final int index)
	{
		set(index, obj);
	}

	public void setSize(final int newSize)
	{
		if (newSize == 0)
			clear();
		else
			throw new IllegalArgumentException();
	}

	public void trimToSize()
	{
	}

	public void addElement(final T obj)
	{
		add(obj);
	}

	public void insertElementAt(final T obj, final int index)
	{
		add(index, obj);
	}

	public void removeAllElements()
	{
		clear();
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public boolean contains(final Object o)
	{
		return indexOf(o) >= 0;
	}

	@Override
	public Iterator<T> iterator()
	{
		@SuppressWarnings("unchecked")
		final Iterator<T> iter = new FilteredIterator<T>(new ConvertingIterator<Pair<T,Long>,T>(list.iterator(),fconv),Filterer.NON_NULL);
		return iter;
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(final T[] a)
	{
		return list.toArray(a);
	}

	@Override
	public boolean add(final T e)
	{
		return list.add(new Pair<T,Long>(e,defTime()));
	}

	public boolean add(final T e, final long expirationMs)
	{
		return list.add(new Pair<T,Long>(e,new Long(System.currentTimeMillis() + expirationMs)));
	}

	@Override
	public boolean remove(final Object o)
	{
		return list.remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends T> c)
	{
		boolean success = true;
		for(final T C : c)
			success = add(C) && success;
		return success;
	}

	public boolean addAll(final Collection<? extends T> c, final long expirationMs)
	{
		boolean success = true;
		for(final T C : c)
			success = add(C,expirationMs) && success;
		return success;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> c)
	{
		for(final T C : c)
			add(index,C);
		return true;
	}

	public boolean addAll(final int index, final Collection<? extends T> c, final long expirationMs)
	{
		for(final T C : c)
			add(index,C,expirationMs);
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		boolean success = true;
		for(final Object o : c)
			success = remove(o) && success;
		return success;
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		final int oldSize = size();
		for(final Iterator<Pair<T,Long>> i = list.iterator(); i.hasNext();)
		{
			final Pair<T,Long> I = i.next();
			if((I != null)
			&&((!c.contains(I.first))||(System.currentTimeMillis()>I.second.longValue())))
				i.remove();
		}
		return oldSize != size();
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public boolean equals(final Object o)
	{
		return list.equals(o);
	}

	@Override
	public int hashCode()
	{
		return list.hashCode();
	}

	@Override
	public T get(final int index)
	{
		final Pair<T,Long> O = list.get(index);
		if((O != null)&&(System.currentTimeMillis()>O.second.longValue()))
		{
			remove(index);
			return get(index);
		}
		return O.first;
	}

	@Override
	public T set(final int index, final T element)
	{
		final Pair<T,Long> O = list.get(index);
		O.first = element;
		O.second = defTime();
		return element;
	}

	@Override
	public void add(final int index, final T element)
	{
		list.add(index,new Pair<T,Long>(element,defTime()));
	}

	public void add(final int index, final T element, final long expirationMs)
	{
		list.add(index,new Pair<T,Long>(element,new Long(System.currentTimeMillis()+expirationMs)));
	}

	@Override
	public T remove(final int index)
	{
		final Pair<T,Long> O = list.remove(index);
		if(O == null)
			return null;
		return O.first;
	}

	@Override
	public int indexOf(final Object o)
	{
		for(int i = 0;i<size();i++)
		{
			try
			{
				final T p = get(i);
				if(p == o)
					return i;
			}
			catch(final java.lang.ArrayIndexOutOfBoundsException x)
			{}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(final Object o)
	{
		for(int i = size();i>=0;i--)
		{
			try
			{
				final T p = get(i);
				if(p == o)
					return i;
			}
			catch(final java.lang.ArrayIndexOutOfBoundsException x)
			{}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator()
	{
		@SuppressWarnings("unchecked")
		final ListIterator<T> iter = new FilteredListIterator<T>(new ConvertingListIterator<Pair<T,Long>,T>(list.listIterator(),fconv),Filterer.NON_NULL);
		return iter;
	}

	@Override
	public ListIterator<T> listIterator(final int index)
	{
		@SuppressWarnings("unchecked")
		final ListIterator<T> iter = new FilteredListIterator<T>(new ConvertingListIterator<Pair<T,Long>,T>(list.listIterator(index),fconv),Filterer.NON_NULL);
		return iter;
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex)
	{
		@SuppressWarnings("unchecked")
		final List<T> lst = new FilteredListWrapper<T>(new ConvertingList<Pair<T,Long>,T>(list.subList(fromIndex, toIndex),fconv),Filterer.NON_NULL);
		return lst;
	}

	public boolean removeElement(final Object obj)
	{
		for(int i = 0;i<size();i++)
		{
			try
			{
				final T p = get(i);
				if(p == obj)
				{
					return remove(i) == obj;
				}
			}
			catch(final java.lang.ArrayIndexOutOfBoundsException x)
			{}
		}
		return false;
	}

	public void removeElementAt(final int index)
	{
		list.remove(index);
	}
}
