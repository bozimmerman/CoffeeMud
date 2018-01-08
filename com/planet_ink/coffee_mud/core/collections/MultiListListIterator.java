package com.planet_ink.coffee_mud.core.collections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;
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
public class MultiListListIterator<K> implements ListIterator<K>
{
	private final List<ListIterator<K>> iters=new ArrayList<ListIterator<K>>();
	private volatile ListIterator<K> iter=null;
	private volatile int listIndex = 0;
	private volatile int itemIndex = 0;

	public MultiListListIterator(ListIterator<K>[] esets)
	{
		if((esets==null)||(esets.length==0))
			return;
		for(final ListIterator<K> I : esets)
			iters.add(I);
	}

	public MultiListListIterator(Collection<ListIterator<K>> esets)
	{
		if((esets==null)||(esets.size()==0))
			return;
		iters.addAll(esets);
		for(final ListIterator<K> I : esets)
			iters.add(I);
	}

	public MultiListListIterator(List<K>[] esets)
	{
		if((esets==null)||(esets.length==0))
			return;
		for(final List<K> I : esets)
			iters.add(I.listIterator());
	}

	public MultiListListIterator(List<? extends List<K>> esets)
	{
		if(esets==null)
			return;
		for(final List<K> I : esets)
			iters.add(I.listIterator());
	}

	public MultiListListIterator()
	{

	}

	public void add(ListIterator<K> eset)
	{
		iters.add(eset);
	}

	@Override
	public boolean hasNext()
	{
		boolean hasNext = (iter != null) && iter.hasNext();
		while(!hasNext)
		{
			if((iters.size()==0)
			||(listIndex<0)
			||(listIndex>=iters.size()))
			{
				iter=null;
				return false;
			}
			iter=iters.get(listIndex++);
			hasNext = (iter != null) && iter.hasNext();
		}
		return hasNext;
	}

	@Override
	public K next()
	{
		if(!hasNext())
			throw new NoSuchElementException();
		itemIndex++;
		return iter.next();
	}

	@Override
	public void remove()
	{
		if(iter != null)
			iter.remove();
	}

	@Override
	public boolean hasPrevious()
	{
		boolean hasPrevious = (iter != null) && iter.hasPrevious();
		while(!hasPrevious)
		{
			if((iters.size()==0)
			||(listIndex<0)
			||(listIndex>=iters.size()))
			{
				iter=null;
				return false;
			}
			iter=iters.get(listIndex--);
			hasPrevious = (iter != null) && iter.hasPrevious();
		}
		return hasPrevious;
	}

	@Override
	public K previous()
	{
		if(!hasPrevious())
			throw new NoSuchElementException();
		itemIndex--;
		return iter.previous();
	}

	@Override
	public int nextIndex()
	{
		return itemIndex;
	}

	@Override
	public int previousIndex()
	{
		return itemIndex-1;
	}

	@Override
	public void set(K e)
	{
		if(hasNext())
			iter.set(e);
		else
		if(hasPrevious())
			iter.set(e);
	}

	@Override
	public void add(K e)
	{
		if(hasNext())
			iter.add(e);
		else
		if(hasPrevious())
			iter.add(e);
	}
}
