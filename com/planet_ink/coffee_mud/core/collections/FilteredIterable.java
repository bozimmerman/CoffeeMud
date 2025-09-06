package com.planet_ink.coffee_mud.core.collections;
import java.util.Iterator;
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
 * An iterable that filters its elements through a Filterer.
 *
 * @param <K> the type of object being filtered
 */
public class FilteredIterable<K> implements Iterable<K>
{
	private final Iterable<K>  iter;
	private Filterer<K>  filterer;

	/**
	 * Construct a new filtered iterable
	 *
	 * @param eset the iterable to wrap
	 * @param fil the filterer to use
	 */
	public FilteredIterable(final Iterable<K> eset, final Filterer<K> fil)
	{
		iter=eset;
		filterer=fil;
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

	@Override
	public Iterator<K> iterator()
	{
		return new FilteredIterator<K>(iter.iterator(),filterer);
	}
}
