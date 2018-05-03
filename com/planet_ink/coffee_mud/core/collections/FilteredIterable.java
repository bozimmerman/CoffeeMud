package com.planet_ink.coffee_mud.core.collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2010-2018 Bo Zimmerman

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
public class FilteredIterable<K> implements Iterable<K>
{
	private final Iterable<K>  iter;
	private Filterer<K>  filterer;

	public FilteredIterable(Iterable<K> eset, Filterer<K> fil)
	{
		iter=eset;
		filterer=fil;
	}

	public void setFilterer(Filterer<K> fil)
	{
		filterer=fil;
	}

	@Override
	public Iterator<K> iterator() {
		return new FilteredIterator<K>(iter.iterator(),filterer);
	}
}
