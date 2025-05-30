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
public class FilteredIterator<K> implements Iterator<K>
{
	private final Iterator<K>	iter;
	private Filterer<K>			filterer;
	private K					nextElement	= null;
	private boolean				initialized	= false;
	private final boolean		delete;

	public FilteredIterator(final Iterator<K> eset, final Filterer<K> fil)
	{
		iter=eset;
		filterer=fil;
		delete=false;
	}

	public FilteredIterator(final Iterator<K> eset, final Filterer<K> fil, final boolean delete)
	{
		iter=eset;
		filterer=fil;
		this.delete=delete;
	}

	public void setFilterer(final Filterer<K> fil)
	{
		filterer=fil;
	}

	private void stageNextElement()
	{
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

	private void initialize()
	{
		if(!initialized)
		{
			stageNextElement();
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

	@Override
	public void remove()
	{
		/*
		 * can't remove because next() is the result of a look-ahead.
		 * by the time next() is called, iter has already next()ed
		 * again, meaning that iter.remove() would remove the
		 * wrong thing
		*/
		throw new NoSuchElementException();
	}
}
