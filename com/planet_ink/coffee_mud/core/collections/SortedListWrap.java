package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * List wrapper for an existing List that turns it into a sorted one.
 * @author Bo Zimmerman
 *
 * @param <T> the type?
 */
public class SortedListWrap<T extends Comparable<T>> implements List<T>
{
	private final List<T> list;
	public SortedListWrap(final List<T> list)
	{
		this.list=list;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected int compareTo(final T arg0, final Object arg1)
	{
		if(arg0 != null)
			return ((Comparable)arg0).compareTo(arg1);
		return -1;
	}

	@Override
	public synchronized boolean add(final T arg0)
	{
		if(arg0==null)
			return false;
		if(list.size()==0)
			return list.add(arg0);
		int start=0;
		int end=list.size()-1;
		int comp=-1;
		int mid=-1;
		while(start<=end)
		{
			mid=(end+start)/2;
			try
			{
				comp=compareTo(list.get(mid),arg0);
				if(comp==0)
					break;
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
			}
			catch(final IndexOutOfBoundsException e)
			{
				comp=-1;
				mid=-1;
				start=0;
				end=list.size()-1;
			}
		}
		if(comp==0)
			list.add(mid,arg0);
		else
		if(comp>0)
		{
			while((mid>=0)&&(compareTo(list.get(mid),arg0)>0))
				mid--;
			if(mid>=list.size()-1)
				list.add(arg0);
			else
			if(mid<0)
				list.add(0,arg0);
			else
				list.add(mid+1,arg0);
		}
		else
		{
			while((mid<list.size())&&(compareTo(list.get(mid),arg0)<0))
				mid++;
			if(mid>=list.size())
				list.add(arg0);
			else
				list.add(mid,arg0);
		}
		return true;
	}

	@Override
	public void add(final int arg0, final T arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public synchronized boolean addAll(final Collection<? extends T> arg0)
	{
		boolean tf=true;
		for(final T t : arg0)
			tf=tf&&add(t);
		return tf;
	}

	@Override
	public boolean addAll(final int arg0, final Collection<? extends T> arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public synchronized void clear()
	{
		list.clear();
	}

	@Override
	public boolean contains(final Object arg0)
	{
		return indexOf(arg0)>=0;
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		for(final Object o : arg0)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public T get(final int arg0)
	{
		return list.get(arg0);
	}

	@Override
	public synchronized int indexOf(final Object arg0)
	{
		if(list.size()==0)
			return -1;
		int start=0;
		int end=list.size()-1;
		while(start<=end)
		{
			final int mid=(end+start)/2;
			try
			{
				final int comp=compareTo(list.get(mid),arg0);
				if(comp==0)
					return mid;
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
			}
			catch(final IndexOutOfBoundsException e)
			{
				start=0;
				end=size()-1;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterator<T> iterator()
	{
		return new ReadOnlyIterator<T>(list.iterator());
	}

	@Override
	public synchronized int lastIndexOf(final Object arg0)
	{
		int firstIndex=indexOf(arg0);
		if(firstIndex<0)
			return -1;
		while((firstIndex<list.size())&&(compareTo(list.get(firstIndex),arg0)==0))
			firstIndex++;
		return firstIndex;
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return new ReadOnlyListIterator<T>(list.listIterator());
	}

	@Override
	public ListIterator<T> listIterator(final int arg0)
	{
		return new ReadOnlyListIterator<T>(list.listIterator(arg0));
	}

	@Override
	public synchronized boolean remove(final Object arg0)
	{
		final int index=indexOf(arg0);
		if(index >= 0)
			return remove(index)==arg0;
		return false;
	}

	@Override
	public synchronized T remove(final int arg0)
	{
		return list.remove(arg0);
	}

	@Override
	public synchronized boolean removeAll(final Collection<?> arg0)
	{
		return list.removeAll(arg0);
	}

	@Override
	public synchronized boolean retainAll(final Collection<?> arg0)
	{
		return list.retainAll(arg0);
	}

	@Override
	public T set(final int arg0, final T arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public List<T> subList(final int arg0, final int arg1)
	{
		return list.subList(arg0, arg1);
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@SuppressWarnings("hiding")

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return list.toArray(arg0);
	}
}
