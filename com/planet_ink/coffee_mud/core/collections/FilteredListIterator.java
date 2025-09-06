package com.planet_ink.coffee_mud.core.collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2012-2025 Bo Zimmerman

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
 * An iterator that filters its elements through a Filterer.
 *
 * @param <K> the type of object being filtered
 */
public class FilteredListIterator<K> implements ListIterator<K>
{
	private final ListIterator<K>  iter;
	private Filterer<K> 	filterer;
	private K 				nextElement = null;
	private K 				prevElement = null;
	private boolean 		initialized = false;
	private final boolean	delete;

	/**
	 * Construct a new filtered iterator
	 *
	 * @param eset the iterator to wrap
	 * @param fil the filterer to use
	 * @param delete true to cause filtered-out elements to be removed from the
	 *            underlying iterator
	 */
	public FilteredListIterator(final ListIterator<K> eset, final Filterer<K> fil, final boolean delete)
	{
		iter=eset;
		filterer=fil;
		this.delete=delete;
	}

	/**
	 * Construct a new filtered iterator
	 *
	 * @param eset the iterator to wrap
	 * @param fil the filterer to use
	 */
	public FilteredListIterator(final ListIterator<K> eset, final Filterer<K> fil)
	{
		iter=eset;
		filterer=fil;
		delete = false;
	}

	/**
	 * Set the filterer to be used
	 *
	 * @param fil the new filterer
	 */
	public void setFilterer(final Filterer<K> fil)
	{
		filterer=fil;
	}

	/**
	 * Stages the next element that passes the filterer, or null if there is
	 * none.
	 */
	private void stageNextElement()
	{
		prevElement=nextElement;
		nextElement = null;
		while((nextElement==null) && (iter.hasNext()))
		{
			nextElement = iter.next();
			if(filterer.passesFilter(nextElement))
				return;
			if(delete)
				iter.remove();
			nextElement = null;
		}
	}

	/**
	 * Stages the previous element that passes the filterer, or null if there is
	 * none.
	 */
	private void stagePrevElement()
	{
		nextElement=prevElement;
		prevElement = null;
		while((prevElement==null) && (iter.hasPrevious()))
		{
			prevElement = iter.previous();
			if(filterer.passesFilter(prevElement))
				return;
			if(delete)
				iter.remove();
			prevElement = null;
		}
	}

	/**
	 * Initializes the iterator by staging the first element
	 */
	private void initialize()
	{
		if(!initialized)
		{
			stageNextElement();
			stagePrevElement();
			initialized=true;
		}
	}

	@Override
	public boolean hasNext()
	{
		if(!initialized)
			initialize();
		return nextElement!=null;
	}

	@Override
	public K next()
	{
		if(!hasNext())
			throw new NoSuchElementException();
		final K element = nextElement;
		stageNextElement();
		return element;
	}

	/**
	 * Not supported, as the result of next() is not the last thing returned by
	 * the underlying iterator
	 */
	@Override
	public void remove()
	{
		/*
		 * can't remove because next() is the result of a look-ahead.
		 * by the time next() is called, iter has already next()ed
		 * again, meaning that iter.remove() would remove the
		 * wrong thing
		*/
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Not supported, as this iterator is read-only
	 */
	@Override
	public void add(final K e)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean hasPrevious()
	{
		if(!initialized)
			initialize();
		return prevElement!=null;
	}

	@Override
	public int nextIndex()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K previous()
	{
		if(!hasPrevious())
			throw new NoSuchElementException();
		final K element = prevElement;
		stagePrevElement();
		return element;
	}

	/**
	 * Not supported, as the result of previous() is not the last thing returned
	 * by the underlying iterator
	 */
	@Override
	public int previousIndex()
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Not supported, as the result of next() is not the last thing returned by
	 * the underlying iterator
	 */
	@Override
	public void set(final K e)
	{
		throw new java.lang.IllegalArgumentException();
	}
}
