package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

import com.planet_ink.coffee_mud.core.CMParms;

/*
   Copyright 2016-2026 Bo Zimmerman

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
/**
 * A MultiSet is a set that contains multiple other set, and presents them
 * as one big set. Adding new items only adds to the last set in the
 * collection. Removing items removes them from the first set they are found
 * in. Iteration and indexing is done as if all the sets were one big set.
 *
 * @param <T> the type of objects in the sets
 * @author Bo Zimmerman
 *
 */
@SuppressWarnings("unchecked")
public class MultiSet<T> implements Set<T>
{
	private final Vector<Set<T>> collections = new Vector<Set<T>>();

	/**
	 * Constructs a new MultiList containing the given lists.
	 *
	 * @param colls the lists to include
	 */
	public MultiSet(final Set<T>... colls)
	{
		super();
		if(colls==null)
			return;
		for(final Set<T> list : colls)
			collections.add(list);
		collections.trimToSize();
	}

	/**
	 * Constructs a new empty MultiSett.
	 */
	public MultiSet()
	{
		super();
	}

	@Override
	public boolean add(final T arg0)
	{
		if(collections.size()>0)
		{
			try
			{
				final Set<T> coll=collections.get(collections.size()-1);
				return coll.add(arg0);
			}
			catch (final Exception e)
			{
			}
		}
		return false;
	}

	/**
	 * Add a new list to the collection of lists.
	 *
	 * @param arg0 the list to add
	 * @return true, always
	 */
	public boolean addAll(final Set<T> arg0)
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
	public boolean contains(final Object arg0)
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
			catch (final Exception e)
			{
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
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
			catch (final Exception e)
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
	public boolean remove(final Object arg0)
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
			catch (final Exception e)
			{
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(final Collection<?> arg0)
	{
		if(collections.size()>0)
		{
			boolean returnable=false;
			try
			{
				for(int c=0;c<collections.size();c++)
					returnable = collections.get(c).removeAll(arg0) || returnable;
			}
			catch (final Exception e)
			{
			}
			return returnable;
		}
		return false;
	}

	@Override
	public boolean retainAll(final Collection<?> arg0)
	{
		if(collections.size()>0)
		{
			boolean returnable=false;
			try
			{
				for(int c=0;c<collections.size();c++)
					returnable = collections.get(c).retainAll(arg0) || returnable;
			}
			catch (final Exception e)
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
			catch (final Exception e)
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
		catch (final Exception e)
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
		for(final Object o : objs)
			arg0[i++]=(T)o;
		return arg0;
	}

	@Override
	public boolean addAll(final Collection<? extends T> c)
	{
		if(c instanceof List)
			collections.add((Set<T>)c);
		else
			collections.add((Set<T>)Arrays.asList(c.toArray()));
		collections.trimToSize();
		return true;
	}
}
