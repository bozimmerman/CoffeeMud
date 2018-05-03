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
   Copyright 2003-2018 Bo Zimmerman

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
	private SVector<Object[]> 	stuff;
	private final static int 	MAX_SIZE=9;

	public DVector(int dim)
	{
		if(dim<1)
			throw new java.lang.IndexOutOfBoundsException();
		if(dim>MAX_SIZE)
			throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new SVector<Object[]>(1);
	}

	public DVector(int dim, int startingSize)
	{
		if(dim<1)
			throw new java.lang.IndexOutOfBoundsException();
		if(dim>MAX_SIZE)
			throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new SVector<Object[]>(startingSize);
	}

	public synchronized void clear()
	{
		stuff.clear();
	}

	public synchronized void trimToSize()
	{
		stuff.trimToSize();
	}

	public synchronized int indexOf(Object O)
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

	public synchronized Object[] elementsAt(int x)
	{
		if((x<0)||(x>=stuff.size()))
			throw new java.lang.IndexOutOfBoundsException();
		return stuff.elementAt(x);
	}

	public synchronized Object[] removeElementsAt(int x)
	{
		if((x<0)||(x>=stuff.size()))
			throw new java.lang.IndexOutOfBoundsException();
		final Object[] O=stuff.elementAt(x);
		stuff.removeElementAt(x);
		return O;
	}

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

	public synchronized void sortBy(int dim)
	{
		if((dim<1)||(dim>dimensions))
			throw new java.lang.IndexOutOfBoundsException();
		dim--;
		if(stuff!=null)
		{
			final TreeSet<Object> sorted=new TreeSet<Object>();
			Object O=null;
			for (final Object[] name : stuff)
			{
				O=(name)[dim];
				if(!sorted.contains(O))
					sorted.add(O);
			}
			final SVector<Object[]> newStuff = new SVector<Object[]>(stuff.size());
			for(final Iterator<Object> i=sorted.iterator();i.hasNext();)
			{
				O=i.next();
				for (final Object[] Os : stuff)
				{
					if(O==Os[dim])
						newStuff.addElement(Os);
				}
			}
			stuff=newStuff;
		}
	}

	public static DVector toNVector(Map<? extends Object,? extends Object> h)
	{
		final DVector DV=new DVector(2);
		for(Object key : h.keySet())
		{
			DV.addElement(key,h.get(key));
		}
		return DV;
	}

	public synchronized void addSharedElements(Object[] O)
	{
		if(dimensions!=O.length)
			throw new java.lang.IndexOutOfBoundsException();
		stuff.addElement(O);
	}

	public synchronized void addElement(Object... Os)
	{
		if(dimensions!=Os.length)
			throw new java.lang.IndexOutOfBoundsException();
		stuff.addElement(Os);
	}

	public synchronized void add(Object... Os)
	{
		if(dimensions!=Os.length)
			throw new java.lang.IndexOutOfBoundsException();
		stuff.addElement(Os);
	}

	public boolean contains(Object O)
	{
		return indexOf(O)>=0;
	}

	public synchronized boolean containsIgnoreCase(String S)
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

	public int size()
	{
		return stuff.size();
	}

	public synchronized void removeElementAt(int i)
	{
		if(i>=0)
			stuff.removeElementAt(i);
	}

	public synchronized void remove(int i)
	{
		if(i>=0)
			stuff.removeElementAt(i);
	}

	public synchronized void removeElement(Object O)
	{
		removeElementAt(indexOf(O));
	}

	public synchronized List<Object> getDimensionList(int dim)
	{
		final Vector<Object> V=new Vector<Object>(stuff.size());
		if(dimensions<dim)
			throw new java.lang.IndexOutOfBoundsException();
		for (final Object[] name : stuff)
			V.addElement(name[dim-1]);
		return V;
	}

	public synchronized List<Object> getRowList(int row)
	{
		final Vector<Object> V=new Vector<Object>(dimensions);
		final Object[] O=elementsAt(row);
		for (final Object element : O)
			V.add(element);
		return V;
	}

	public synchronized Object elementAt(int i, int dim)
	{
		if(dimensions<dim)
			throw new java.lang.IndexOutOfBoundsException();
		return (stuff.elementAt(i))[dim-1];
	}

	public synchronized Object get(int i, int dim)
	{
		if(dimensions<dim)
			throw new java.lang.IndexOutOfBoundsException();
		return (stuff.elementAt(i))[dim-1];
	}

	public synchronized void setElementAt(int index, int dim, Object O)
	{
		if(dimensions<dim)
			throw new java.lang.IndexOutOfBoundsException();
		stuff.elementAt(index)[dim-1]=O;
	}

	public synchronized void set(int index, int dim, Object O)
	{
		if(dimensions<dim)
			throw new java.lang.IndexOutOfBoundsException();
		stuff.elementAt(index)[dim-1]=O;
	}

	public synchronized void insertElementAt(int here, Object... Os)
	{
		if(dimensions!=Os.length)
			throw new java.lang.IndexOutOfBoundsException();
		stuff.insertElementAt(Os,here);
	}
	
	public synchronized void add(int here, Object... Os)
	{
		if(dimensions!=Os.length)
			throw new java.lang.IndexOutOfBoundsException();
		stuff.insertElementAt(Os,here);
	}
}
