package com.planet_ink.coffee_mud.core.collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;

/*
   Copyright 2012-2026 Bo Zimmerman

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
	private final ListIterator<K>	iter;
	private Filterer<K>				filterer;
	private final boolean			delete;

	/**
	 * Construct a new filtered iterator
	 *
	 * @param eset the iterator to wrap
	 * @param fil the filterer to use
	 * @param delete true to cause filtered-out elements to be removed from the
	 *            underlying iterator during next() or previous()
	 */
	public FilteredListIterator(final ListIterator<K> eset, final Filterer<K> fil, final boolean delete)
	{
		iter = eset;
		filterer = fil;
		this.delete = delete;
	}

	/**
	 * Construct a new filtered iterator
	 *
	 * @param eset the iterator to wrap
	 * @param fil the filterer to use
	 */
	public FilteredListIterator(final ListIterator<K> eset, final Filterer<K> fil)
	{
		this(eset, fil, false);
	}

	/**
	 * Set the filterer to be used
	 *
	 * @param fil the new filterer
	 */
	public void setFilterer(final Filterer<K> fil)
	{
		filterer = fil;
	}

	/**
	 * Peeks forward to find the index of the next passing element without
	 * modifying the iterator position or list.
	 *
	 * @return the underlying index of the next passing element, or -1 if none
	 */
	private int peekNextIndex()
	{
		K temp;
		boolean found = false;
		int targetIndex = -1;
		int advances = 0;
		while (iter.hasNext())
		{
			temp = iter.next();
			advances++;
			if (filterer.passesFilter(temp))
			{
				targetIndex = iter.previousIndex();
				found = true;
				break;
			}
		}
		// Retreat to original position
		for (int i = 0; i < advances; i++)
			iter.previous();
		return found ? targetIndex : -1;
	}

	/**
	 * Peeks backward to find the index of the previous passing element without
	 * modifying the iterator position or list.
	 *
	 * @return the underlying index of the previous passing element, or -1 if
	 *         none
	 */
	private int peekPreviousIndex()
	{
		K temp;
		boolean found = false;
		int targetIndex = -1;
		int retreats = 0;
		while (iter.hasPrevious())
		{
			temp = iter.previous();
			retreats++;
			if (filterer.passesFilter(temp))
			{
				targetIndex = iter.nextIndex();
				found = true;
				break;
			}
		}
		// Advance back to original position
		for (int i = 0; i < retreats; i++)
			iter.next();
		return found ? targetIndex : -1;
	}

	@Override
	public boolean hasNext()
	{
		return peekNextIndex() >= 0;
	}

	@Override
	public boolean hasPrevious()
	{
		return peekPreviousIndex() >= 0;
	}

	@Override
	public int nextIndex()
	{
		return peekNextIndex();
	}

	@Override
	public int previousIndex()
	{
		return peekPreviousIndex();
	}

	@Override
	public K next()
	{
		if (!hasNext())
			throw new NoSuchElementException();
		K elem;
		while (iter.hasNext())
		{
			elem = iter.next();
			if (filterer.passesFilter(elem))
				return elem;
			else
			if (delete)
				iter.remove();
		}
		throw new NoSuchElementException();
	}

	@Override
	public K previous()
	{
		if (!hasPrevious())
			throw new NoSuchElementException();
		K elem;
		while (iter.hasPrevious())
		{
			elem = iter.previous();
			if (filterer.passesFilter(elem))
				return elem;
			else if (delete)
				iter.remove();
		}
		throw new NoSuchElementException();
	}

	@Override
	public void remove()
	{
		iter.remove();
	}

	@Override
	public void set(final K e)
	{
		iter.set(e);
	}

	@Override
	public void add(final K e)
	{
		iter.add(e);
	}
}