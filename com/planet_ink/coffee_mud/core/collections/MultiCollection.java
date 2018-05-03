package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

import com.planet_ink.coffee_mud.core.CMParms;

/*
   Copyright 2014-2018 Bo Zimmerman

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
public class MultiCollection<T> implements Collection<T>
{
	private final Vector<Collection<? extends T>> collections = new Vector<Collection<? extends T>>();
	
	public MultiCollection(final Collection<T>... colls)
	{
		super();
		if(colls==null)
			return;
		collections.addAll(Arrays.asList(colls));
		collections.trimToSize();
	}

	@Override
	public boolean add(T arg0) 
	{
		if(collections.size()>0)
		{
			try
			{
				final Collection<T> coll=(Collection<T>)collections.get(collections.size()-1);
				return coll.add(arg0);
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}

	@Override
	public boolean addAll(final Collection<? extends T> arg0) 
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

}
