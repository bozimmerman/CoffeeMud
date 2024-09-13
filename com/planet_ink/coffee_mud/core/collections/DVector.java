package com.planet_ink.coffee_mud.core.collections;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2024 Bo Zimmerman

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
public class DVector implements Cloneable, NList<Object>, java.io.Serializable
{
	public static final long 	serialVersionUID=43353454350L;
	protected int 				dimensions=1;
	private final SVector<Object[]> 	stuff;
	private final static int 	MAX_SIZE=10;

	public final static DVector empty = new DVector(1);

	public DVector(final int dim)
	{
		if(dim<1)
			throw new IndexOutOfBoundsException();
		if(dim>MAX_SIZE)
			throw new IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new SVector<Object[]>(1);
	}

	public DVector(final int dim, final int startingSize)
	{
		if(dim<1)
			throw new IndexOutOfBoundsException();
		if(dim>MAX_SIZE)
			throw new IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new SVector<Object[]>(startingSize);
	}

	@Override
	public synchronized void clear()
	{
		stuff.clear();
	}

	@Override
	public synchronized void trimToSize()
	{
		stuff.trimToSize();
	}

	@Override
	public synchronized int indexOf(final Object O)
	{
		int x=0;
		if(O==null)
		{
			for(final Enumeration<Object[]> e=stuff.elements();e.hasMoreElements();x++)
				if(e.nextElement()[0]==null)
					return x;
		}
		else
		for(final Enumeration<Object[]> e=stuff.elements();e.hasMoreElements();x++)
		{
			if(O.equals(e.nextElement()[0]))
				return x;
		}
		return -1;
	}

	@Override
	public synchronized Object[] elementsAt(final int x)
	{
		if((x<0)||(x>=stuff.size()))
			throw new IndexOutOfBoundsException();
		return stuff.elementAt(x);
	}

	@Override
	public synchronized Object[] removeElementsAt(final int x)
	{
		if((x<0)||(x>=stuff.size()))
			throw new IndexOutOfBoundsException();
		final Object[] O=stuff.elementAt(x);
		stuff.removeElementAt(x);
		return O;
	}

	@Override
	public synchronized DVector copyOf()
	{
		final DVector V=new DVector(dimensions);
		if(stuff!=null)
		{
			for (final Object[] name : stuff)
				V.stuff.addElement(name.clone());
		}
		return V;
	}

	@Override
	public synchronized void sortBy(int dim)
	{
		if((dim<1)||(dim>dimensions))
			throw new IndexOutOfBoundsException();
		dim--;
		if(stuff!=null)
		{
			final int d=dim;
			final List<Object[]> subStuff=new ArrayList<Object[]>(stuff.size());
			subStuff.addAll(stuff);
			Collections.sort(subStuff,new Comparator<Object[]>(){
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public int compare(final Object[] o1, final Object[] o2)
				{
					final Object oo1=o1[d];
					final Object oo2=o2[d];
					if(oo1 == oo2)
						return 0;
					if(oo1==null)
					{
						if(oo2==null)
							return 0;
						return -1;
					}
					else
					if(oo2==null)
						return 1;
					if(oo1 instanceof Comparable)
						return ((Comparable)oo1).compareTo(oo2);
					return Integer.valueOf(oo1.hashCode()).compareTo(Integer.valueOf(oo2.hashCode()));
				}
			});
			stuff.clear();
			stuff.addAll(subStuff);
		}
	}

	public synchronized void sortBy(final Comparator<Object[]> comparator)
	{
		if(stuff!=null)
		{
			final List<Object[]> subStuff=new ArrayList<Object[]>(stuff.size());
			subStuff.addAll(stuff);
			Collections.sort(subStuff,comparator);
			stuff.clear();
			stuff.addAll(subStuff);
		}
	}

	public static DVector toNVector(final Map<? extends Object,? extends Object> h)
	{
		final DVector DV=new DVector(2);
		for(final Object key : h.keySet())
		{
			DV.addElement(key,h.get(key));
		}
		return DV;
	}

	@Override
	public synchronized void addSharedElements(final Object[] O)
	{
		if(dimensions!=O.length)
			throw new IndexOutOfBoundsException();
		stuff.addElement(O);
	}

	public synchronized void addElement(final Object... Os)
	{
		if(dimensions!=Os.length)
			throw new IndexOutOfBoundsException();
		stuff.addElement(Os);
	}

	public synchronized void add(final Object... Os)
	{
		if(dimensions!=Os.length)
			throw new IndexOutOfBoundsException();
		stuff.addElement(Os);
	}

	@Override
	public boolean contains(final Object O)
	{
		return indexOf(O)>=0;
	}

	@Override
	public synchronized boolean containsIgnoreCase(final String S)
	{
		if(S==null)
			return indexOf(null)>=0;
		for (final Object[] name : stuff)
		{
			if(S.equalsIgnoreCase(name[0].toString()))
				return true;
		}
		return false;
	}

	@Override
	public int size()
	{
		return stuff.size();
	}

	@Override
	public synchronized void removeElementAt(final int i)
	{
		if(i>=0)
			stuff.removeElementAt(i);
	}

	@Override
	public synchronized void remove(final int i)
	{
		if(i>=0)
			stuff.removeElementAt(i);
	}

	@Override
	public synchronized void removeElement(final Object O)
	{
		removeElementAt(indexOf(O));
	}

	@Override
	public synchronized List<Object> getDimensionList(final int dim)
	{
		final Vector<Object> V=new Vector<Object>(stuff.size());
		if(dimensions<dim)
			throw new IndexOutOfBoundsException();
		for (final Object[] name : stuff)
			V.addElement(name[dim-1]);
		return V;
	}

	@Override
	public synchronized List<Object> getRowList(final int row)
	{
		final Vector<Object> V=new Vector<Object>(dimensions);
		final Object[] O=elementsAt(row);
		for (final Object element : O)
			V.add(element);
		return V;
	}

	@Override
	public synchronized Object elementAt(final int i, final int dim)
	{
		if(dimensions<dim)
			throw new IndexOutOfBoundsException();
		return (stuff.elementAt(i))[dim-1];
	}

	@Override
	public synchronized Object get(final int i, final int dim)
	{
		if(dimensions<dim)
			throw new IndexOutOfBoundsException();
		return (stuff.elementAt(i))[dim-1];
	}

	@Override
	public synchronized void setElementAt(final int index, final int dim, final Object O)
	{
		if(dimensions<dim)
			throw new IndexOutOfBoundsException();
		stuff.elementAt(index)[dim-1]=O;
	}

	@Override
	public synchronized void set(final int index, final int dim, final Object O)
	{
		if(dimensions<dim)
			throw new IndexOutOfBoundsException();
		stuff.elementAt(index)[dim-1]=O;
	}

	public synchronized void insertElementAt(final int here, final Object... Os)
	{
		if(dimensions!=Os.length)
			throw new IndexOutOfBoundsException();
		stuff.insertElementAt(Os,here);
	}

	public synchronized void add(final int here, final Object... Os)
	{
		if(dimensions!=Os.length)
			throw new IndexOutOfBoundsException();
		stuff.insertElementAt(Os,here);
	}
}
