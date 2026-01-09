package com.planet_ink.coffee_mud.core.collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2010-2026 Bo Zimmerman

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
 * @param <K> the type of object being filtered
 */
public class FilteredIterator<K> implements Iterator<K>
{
	private final Iterator<K>	iter;
	private Filterer<K>			filterer;
	private K					nextElement	= null;
	private final boolean		delete;

	/**
	 * Construct a new filtered iterator
	 *
	 * @param eset the iterator to wrap
	 * @param fil the filterer to use
	 */
	public FilteredIterator(final Iterator<K> eset, final Filterer<K> fil)
	{
		iter = eset;
		filterer = fil;
		delete = false;
	}

	/**
	 * Construct a new filtered iterator
	 *
	 * @param eset the iterator to wrap
	 * @param fil the filterer to use
	 * @param delete true to cause filtered-out elements to be removed from the
	 *            underlying iterator
	 */
	public FilteredIterator(final Iterator<K> eset, final Filterer<K> fil, final boolean delete)
	{
		iter = eset;
		filterer = fil;
		this.delete = delete;
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
	 * Stages the next element that passes the filterer, or null if there is
	 * none.
	 */
	private void stageNextElement()
	{
		nextElement = null;
		while ((nextElement == null) && (iter.hasNext()))
		{
			nextElement = iter.next();
			if (filterer.passesFilter(nextElement))
				return;
			if (delete)
				iter.remove();
			nextElement = null;
		}
	}

	@Override
	public boolean hasNext()
	{
		if (nextElement == null)
			stageNextElement();
		return nextElement != null;
	}

	@Override
	public K next()
	{
		if (!hasNext())
			throw new NoSuchElementException();
		final K element = nextElement;
		nextElement = null;
		return element;
	}

	@Override
	public void remove()
	{
		iter.remove();
	}
}