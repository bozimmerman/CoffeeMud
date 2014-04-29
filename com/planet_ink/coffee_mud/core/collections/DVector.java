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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class DVector implements Cloneable, java.io.Serializable
{
	public static final long 	serialVersionUID=43353454350L;
	protected int 				dimensions=1;
	private SVector<Object[]> 	stuff;
	private final static int 	MAX_SIZE=9;

	public DVector(int dim)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		if(dim>MAX_SIZE) throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new SVector<Object[]>(1);
	}
	public DVector(int dim, int startingSize)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		if(dim>MAX_SIZE) throw new java.lang.IndexOutOfBoundsException();
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
			for(Enumeration<Object[]> e=stuff.elements();e.hasMoreElements();x++)
				if(e.nextElement()[0]==null)
					return x;
		}
		else
		for(Enumeration<Object[]> e=stuff.elements();e.hasMoreElements();x++)
			if(O.equals(e.nextElement()[0]))
				return x;
		return -1;
	}
	public synchronized Object[] elementsAt(int x)
	{
		if((x<0)||(x>=stuff.size())) throw new java.lang.IndexOutOfBoundsException();
		return stuff.elementAt(x);
	}

	public synchronized Object[] removeElementsAt(int x)
	{
		if((x<0)||(x>=stuff.size())) throw new java.lang.IndexOutOfBoundsException();
		Object[] O=stuff.elementAt(x);
		stuff.removeElementAt(x);
		return O;
	}

	public synchronized DVector copyOf()
	{
		DVector V=new DVector(dimensions);
		if(stuff!=null)
		{
			for(Enumeration<Object[]> s=stuff.elements();s.hasMoreElements();)
				V.stuff.addElement(s.nextElement().clone());
		}
		return V;
	}

	public synchronized void sortBy(int dim)
	{
		if((dim<1)||(dim>dimensions)) throw new java.lang.IndexOutOfBoundsException();
		dim--;
		if(stuff!=null)
		{
			TreeSet sorted=new TreeSet();
			Object O=null;
			for(Enumeration<Object[]> s=stuff.elements();s.hasMoreElements();)
			{
				O=(s.nextElement())[dim];
				if(!sorted.contains(O))
					sorted.add(O);
			}
			SVector<Object[]> newStuff = new SVector<Object[]>(stuff.size());
			for(Iterator i=sorted.iterator();i.hasNext();)
			{
				O=i.next();
				for(Enumeration<Object[]> s=stuff.elements();s.hasMoreElements();)
				{
					Object[] Os=s.nextElement();
					if(O==Os[dim]) newStuff.addElement(Os);
				}
			}
			stuff=newStuff;
		}
	}

	public static DVector toDVector(Hashtable h)
	{
		DVector DV=new DVector(2);
		for(Enumeration e=h.keys();e.hasMoreElements();)
		{
			Object key=e.nextElement();
			DV.addElement(key,h.get(key));
		}
		return DV;
	}

	public synchronized void addSharedElements(Object[] O)
	{
		if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
		stuff.addElement(O);
	}
	public synchronized void addElement(Object... Os)
	{
		if(dimensions!=Os.length) throw new java.lang.IndexOutOfBoundsException();
		stuff.addElement(Os);
	}
	public boolean contains(Object O)
	{
		return indexOf(O)>=0;
	}
	public synchronized boolean containsIgnoreCase(String S)
	{
		if(S==null) return indexOf(null)>=0;
		for(Enumeration<Object[]> e=stuff.elements();e.hasMoreElements();)
			if(S.equalsIgnoreCase(e.nextElement()[0].toString()))
				return true;
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
	public synchronized void removeElement(Object O)
	{
		removeElementAt(indexOf(O));
	}
	public synchronized Vector getDimensionVector(int dim)
	{
		Vector V=new Vector<Object>(stuff.size());
		if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
		for(Enumeration<Object[]> e=stuff.elements();e.hasMoreElements();)
			V.addElement(e.nextElement()[dim-1]);
		return V;
	}
	public synchronized Vector getRowVector(int row)
	{
		Vector V=new Vector<Object>(dimensions);
		Object[] O=elementsAt(row);
		for(int v=0;v<O.length;v++)
			V.add(O[v]);
		return V;
	}
	public synchronized Object elementAt(int i, int dim)
	{
		if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
		return (stuff.elementAt(i))[dim-1];
	}

	public synchronized void setElementAt(int index, int dim, Object O)
	{
		if(dimensions<dim) throw new java.lang.IndexOutOfBoundsException();
		stuff.elementAt(index)[dim-1]=O;
	}

	public synchronized void insertElementAt(int here, Object... Os)
	{
		if(dimensions!=Os.length) throw new java.lang.IndexOutOfBoundsException();
		stuff.insertElementAt(Os,here);
	}
}
