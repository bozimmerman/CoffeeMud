package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2012-2024 Bo Zimmerman

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
public class CMUniqSortListWrapper<T extends CMObject> implements SearchIDList<T>
{
	private boolean readOnly = false;
	private final List<T> list;

	public CMUniqSortListWrapper(final List<T> list)
	{
		this.list=list;
	}

	protected int compareTo(final CMObject arg0, final String arg1)
	{
		return arg0.ID().compareToIgnoreCase(arg1);
	}

	protected int compareToStarts(final CMObject arg0, final String arg1)
	{
		if(arg0.ID().toLowerCase().startsWith(arg1.toLowerCase()))
			return 0;
		return arg0.ID().compareToIgnoreCase(arg1);
	}

	protected int compareToLowerStarts(final CMObject arg0, final String arg1)
	{
		if(arg0.ID().toLowerCase().startsWith(arg1))
			return 0;
		return arg0.ID().compareToIgnoreCase(arg1);
	}

	protected int compareTo(final CMObject arg0, final CMObject arg1)
	{
		return arg0.ID().compareToIgnoreCase(arg1.ID());
	}

	@Override
	public synchronized boolean add(final T arg0)
	{
		if((arg0==null)||(readOnly))
			return false;
		if(size()==0)
			return list.add(arg0);
		int start=0;
		int end=size()-1;
		int comp=-1;
		int mid=-1;
		while(start<=end)
		{
			mid=(end+start)/2;
			try
			{
				comp=compareTo(list.get(mid),arg0);
				if(comp==0)
					return false;
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
				end=size()-1;
			}
		}
		if(comp==0)
			list.add(mid,arg0);
		else
		if(comp>0)
		{
			while((mid>=0)&&(compareTo(list.get(mid),arg0)>0))
				mid--;
			if(mid>=size()-1)
				list.add(arg0);
			else
			if(mid<0)
				list.add(0,arg0);
			else
				list.add(mid+1,arg0);
		}
		else
		{
			while((mid<size())&&(compareTo(list.get(mid),arg0)<0))
				mid++;
			if(mid>=size())
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
	public boolean addAll(final int arg0, final Collection<? extends T> arg1)
	{
		throw new java.lang.UnsupportedOperationException();
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
	public Iterator<String> keyIterator()
	{
		return new ConvertingIterator<T,String>(this.iterator(),new Converter<T,String>()
		{
			@Override
			public String convert(final T obj)
			{
				if(obj!=null)
					return obj.ID();
				return null;
			}
		});
	}

	@Override
	public T get(final int arg0)
	{
		return list.get(arg0);
	}

	@Override
	public synchronized int indexOf(final Object arg0)
	{
		if(arg0==null)
			return -1;
		if(size()==0)
			return -1;
		int start=0;
		int end=size()-1;
		if(arg0 instanceof CMObject)
		{
			while(start<=end)
			{
				final int mid=(end+start)/2;
				try
				{
					final int comp=compareTo(list.get(mid),(CMObject)arg0);
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
		}
		else
		if(arg0 instanceof String)
		{
			while(start<=end)
			{
				final int mid=(end+start)/2;
				try
				{
					final int comp=compareTo(list.get(mid),(String)arg0);
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
		}
		return -1;
	}

	@Override
	public synchronized T find(final String arg0)
	{
		if(arg0==null)
			return null;
		if(size()==0)
			return null;
		int start=0;
		int end=size()-1;
		while(start<=end)
		{
			final int mid=(end+start)/2;
			try
			{
				final int comp=compareTo(list.get(mid),arg0);
				if(comp==0)
					return list.get(mid);
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
		return null;
	}

	public synchronized T findStartsWith(final String arg0)
	{
		if(arg0==null)
			return null;
		if(size()==0)
			return null;
		final String larg0 = arg0.toLowerCase();
		int start=0;
		int end=size()-1;
		while(start<=end)
		{
			final int mid=(end+start)/2;
			try
			{
				final int comp=compareToLowerStarts(list.get(mid), larg0);
				if(comp==0)
					return list.get(mid);
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
		return null;
	}

	@Override
	public synchronized T find(final CMObject arg0)
	{
		if(arg0==null)
			return null;
		if(size()==0)
			return null;
		int start=0;
		int end=size()-1;
		while(start<=end)
		{
			final int mid=(end+start)/2;
			try
			{
				final int comp=compareTo(list.get(mid),arg0);
				if(comp==0)
					return list.get(mid);
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
		return null;
	}

	@Override
	public synchronized int lastIndexOf(final Object arg0)
	{
		return indexOf(arg0); // only holds one-of-a-kind, so all is well!
	}

	@Override
	public synchronized boolean remove(final Object arg0)
	{
		final int index=indexOf(arg0);
		if((index >= 0)&&(!readOnly))
			return remove(index)==arg0;
		return false;
	}

	@Override
	public T set(final int arg0, final T arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	public void setReadOnly(final boolean trueFalse)
	{
		readOnly = trueFalse;
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
	public Iterator<T> iterator()
	{
		return list.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@Override
	public <F> F[] toArray(final F[] a)
	{
		return list.toArray(a);
	}

	@Override
	public boolean addAll(final Collection<? extends T> c)
	{
		return list.addAll(c);
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return list.retainAll(c);
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public T remove(final int index)
	{
		return list.remove(index);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(final int index)
	{
		return list.listIterator(index);
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex)
	{
		return list.subList(fromIndex, toIndex);
	}
}
