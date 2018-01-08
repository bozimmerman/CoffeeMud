package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

import com.planet_ink.coffee_mud.core.CMParms;

/*
   Copyright 2016-2018 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class MultiList<T> implements List<T>
{
	private final Vector<List<T>> collections = new Vector<List<T>>();
	
	public MultiList(final List<T>... colls)
	{
		super();
		if(colls==null)
			return;
		for(List<T> list : colls)
			collections.add(list);
		collections.trimToSize();
	}

	public MultiList()
	{
		super();
	}

	@Override
	public boolean add(T arg0) 
	{
		if(collections.size()>0)
		{
			try
			{
				final List<T> coll=collections.get(collections.size()-1);
				return coll.add(arg0);
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}

	public boolean addAll(final List<T> arg0) 
	{
		collections.add(arg0);
		collections.trimToSize();
		return true;
	}

	@Override
	public void clear() 
	{
		collections.clear();
	}

	@Override
	public boolean contains(Object arg0) 
	{
		if(collections.size()>0)
		{
			try
			{
				for(int c=0;c<collections.size();c++)
				{
					if(collections.get(c).contains(arg0))
						return true;
				}
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) 
	{
		for(final Object arg : arg0)
		{
			if(!contains(arg))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() 
	{
		if(collections.size()>0)
		{
			try
			{
				for(int c=0;c<collections.size();c++)
				{
					if(collections.get(c).size()>0)
						return false;
				}
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}

	@Override
	public Iterator<T> iterator() 
	{
		return new MultiIterable<T>(collections,size()).iterator();
	}

	@Override
	public boolean remove(Object arg0) 
	{
		if(collections.size()>0)
		{
			try
			{
				for(int c=0;c<collections.size();c++)
				{
					if(collections.get(c).remove(arg0))
						return true;
				}
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) 
	{
		if(collections.size()>0)
		{
			boolean returnable=false;
			try
			{
				for(int c=0;c<collections.size();c++)
					returnable = collections.get(c).removeAll(arg0) || returnable;
			}
			catch (Exception e)
			{
			}
			return returnable;
		}
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> arg0)
	{
		if(collections.size()>0)
		{
			for(Object o : arg0)
				this.add(index,(T)o);
		}
		return true;
	}
	
	@Override
	public boolean retainAll(Collection<?> arg0) 
	{
		if(collections.size()>0)
		{
			boolean returnable=false;
			try
			{
				for(int c=0;c<collections.size();c++)
					returnable = collections.get(c).retainAll(arg0) || returnable;
			}
			catch (Exception e)
			{
			}
			return returnable;
		}
		return false;
	}

	@Override
	public int size() 
	{
		int total=0;
		if(collections.size()>0)
		{
			try
			{
				for(int c=0;c<collections.size();c++)
					total += collections.get(c).size();
			}
			catch (Exception e)
			{
			}
		}
		return total;
	}

	@Override
	public Object[] toArray() 
	{
		final Object[][] arrays=new Object[collections.size()][];
		try
		{
			for(int c=0;c<collections.size();c++)
				arrays[c]=collections.get(c).toArray();
		}
		catch (Exception e)
		{
		}
		return CMParms.combine(arrays);
	}

	@SuppressWarnings("hiding")

	@Override
	public <T> T[] toArray(T[] arg0) 
	{
		final Object[] objs=toArray();
		if(arg0.length<objs.length)
			arg0=Arrays.copyOf(arg0, objs.length);
		int i=0;
		for(Object o : objs)
			arg0[i++]=(T)o;
		return arg0;
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		if(c instanceof List)
			collections.add((List<T>)c);
		else
			collections.add((List<T>)Arrays.asList(c.toArray()));
		collections.trimToSize();
		return true;
	}

	@Override
	public T get(int index)
	{
		for(List<T> list : collections)
		{
			if(index < list.size())
				return list.get(index);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException ();
	}

	@Override
	public T set(int index, T element)
	{
		for(List<T> list : collections)
		{
			if(index < list.size())
				return list.set(index,element);
			index -= list.size();
		}
		throw new IndexOutOfBoundsException ();
	}

	@Override
	public void add(int index, T element)
	{
		if(collections.size()==0)
			collections.add(new Vector<T>());
		List<T> list = null;
		for(int t=0;t<collections.size();t++)
		{
			list=collections.get(t);
			if(index < list.size())
			{
				list.add(index,element);
				return;
			}
			else
			if(index == list.size())
			{
				list.add(element);
				return;
			}
			
			index -= list.size();
		}
	}

	@Override
	public T remove(int index)
	{
		for(List<T> list : collections)
		{
			if(index < list.size())
				return list.remove(index);
			index -= list.size();
		}
		return null;
	}

	@Override
	public int indexOf(Object o)
	{
		int indexBase = 0;
		for(List<T> list : collections)
		{
			int x=list.indexOf(o);
			if(x>=0)
				return indexBase+x;
			indexBase += list.size();
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o)
	{
		int indexBase = size();
		for(int x=collections.size()-1;x>=0;x--)
		{
			final List<T> list = collections.get(x);
			indexBase -= list.size();
			int y=list.lastIndexOf(o);
			if(y>=0)
				return indexBase+y;
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return new MultiListListIterator<T>(collections);
	}

	@Override
	public ListIterator<T> listIterator(int index)
	{
		ListIterator<T> iter=listIterator();
		while(iter.nextIndex() < index)
			iter.next();
		return iter;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		
		// TODO Auto-generated method stub
		return null;
	}

}
