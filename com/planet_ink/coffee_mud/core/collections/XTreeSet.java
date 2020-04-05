package com.planet_ink.coffee_mud.core.collections;
import java.io.Serializable;
import java.util.*;

/*
   Copyright 2016-2020 Bo Zimmerman

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
 * A version of the Vector class that provides to "safe" adds
 * and removes by copying the underlying vector whenever those
 * operations are done.
 */
public class XTreeSet<T> extends HashSet<T>
{
	private static final long serialVersionUID = 6587178785122563992L;

	public XTreeSet(final List<T> V)
	{
		super();
		if(V!=null)
			addAll(V);
	}

	public XTreeSet(final T[] E)
	{
		super();
		if(E!=null)
			for(final T o : E)
				add(o);
	}

	public XTreeSet(final T E)
	{
		super();
		if(E!=null)
			add(E);
	}

	public XTreeSet()
	{
		super();
	}

	public XTreeSet(final Set<T> E)
	{
		super();
		if(E!=null)
		{
			for(final T o : E)
				add(o);
		}
	}

	public XTreeSet(final Enumeration<T> E)
	{
		super();
		if(E!=null)
		{
			for(;E.hasMoreElements();)
				add(E.nextElement());
		}
	}

	public XTreeSet(final Iterator<T> E)
	{
		super();
		if(E!=null)
		{
			for(;E.hasNext();)
				add(E.next());
		}
	}

	public synchronized void addAll(final Enumeration<T> E)
	{
		if(E!=null)
		{
			for(;E.hasMoreElements();)
				add(E.nextElement());
		}
	}

	public synchronized void addAll(final T[] E)
	{
		if(E!=null)
		{
			for(final T e : E)
				add(e);
		}
	}

	public synchronized void addAll(final Iterator<T> E)
	{
		if(E!=null)
		{
			for(;E.hasNext();)
				add(E.next());
		}
	}

	public synchronized void removeAll(final Enumeration<T> E)
	{
		if(E!=null)
		{
			for(;E.hasMoreElements();)
				remove(E.nextElement());
		}
	}

	public synchronized void removeAll(final Iterator<T> E)
	{
		if(E!=null)
		{
			for(;E.hasNext();)
				remove(E.next());
		}
	}

	public synchronized void removeAll(final List<T> E)
	{
		if(E!=null)
		{
			for(final T o : E)
				remove(o);
		}
	}
	
	public boolean containsAny(Collection<T> C)
	{
		for(final T c : C)
			if(contains(c))
				return true;
		return false;
	}
	
	public boolean containsAny(Enumeration<T> c)
	{
		for(;c.hasMoreElements();)
			if(contains(c.nextElement()))
				return true;
		return false;
	}
	
}
