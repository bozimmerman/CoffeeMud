package com.planet_ink.coffee_mud.core.collections;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2010-2025 Bo Zimmerman

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
 * An iterator that iterates across multiple iterators in sequence.
 *
 * @param <K> the type of object being iterated over
 */
public class MultiIterator<K> implements Iterator<K>
{
	private final LinkedList<Iterator<K>> iters=new LinkedList<Iterator<K>>();
	private volatile Iterator<K> iter=null;

	/**
	 * Construct a new multi-iterator
	 *
	 * @param esets the array of iterators to iterate across
	 */
	public MultiIterator(final Iterator<K>[] esets)
	{
		if((esets==null)||(esets.length==0))
			return;
		for(final Iterator<K> I : esets)
			iters.add(I);
	}

	/**
	 * Construct a new multi-iterator
	 *
	 * @param esets the collection of iterators to iterate across
	 */
	public MultiIterator(final Collection<Iterator<K>> esets)
	{
		if((esets==null)||(esets.size()==0))
			return;
		iters.addAll(esets);
		for(final Iterator<K> I : esets)
			iters.add(I);
	}

	/**
	 * Construct a new multi-iterator
	 *
	 * @param esets the array of iterables to iterate across
	 */
	public MultiIterator(final Iterable<K>[] esets)
	{
		if((esets==null)||(esets.length==0))
			return;
		for(final Iterable<K> I : esets)
			iters.add(I.iterator());
	}

	/**
	 * Construct a new multi-iterator
	 *
	 * @param esets the collection of iterables to iterate across
	 */
	public MultiIterator(final Iterable<? extends Iterable<K>> esets)
	{
		if(esets==null)
			return;
		for(final Iterable<K> I : esets)
			iters.add(I.iterator());
	}

	/**
	 * Construct a new multi-iterator
	 */
	public MultiIterator()
	{

	}

	/**
	 * Add another iterator to the end of this multi-iterator
	 *
	 * @param eset the iterator to add
	 */
	public void add(final Iterator<K> eset)
	{
		iters.add(eset);
	}

	@Override
	public boolean hasNext()
	{
		boolean hasNext = (iter != null) && iter.hasNext();
		while(!hasNext)
		{
			if(iters.size()==0)
			{
				iter=null;
				return false;
			}
			iter=iters.removeFirst();
			hasNext = (iter != null) && iter.hasNext();
		}
		return hasNext;
	}

	@Override
	public K next()
	{
		if(!hasNext())
			throw new NoSuchElementException();
		return iter.next();
	}

	@Override
	public void remove()
	{
		if(iter != null)
			iter.remove();
	}
}
