package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

/*
   Copyright 2013-2025 Bo Zimmerman

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
 * A SortedListWrap which uses a custom comparator to sort the list.
 *
 * @param <T> the type of object in the list
 */
public class CustSortListWrap<T extends Comparable<T>> extends SortedListWrap<T>
{
	private final Comparator<T> comparator;

	/**
	 * Construct a new CustSortListWrap
	 * @param list the list to wrap
	 * @param comparator the comparator to use
	 */
	public CustSortListWrap(final List<T> list, final Comparator<T> comparator)
	{
		super(list);
		this.comparator=comparator;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected int compareTo(final T arg0, final Object arg1)
	{

		if(arg0 == null)
		{
			if(arg1 == null)
				return 0;
			return -1;
		}
		else
		if(arg1 == null)
			return 1;
		else
			return comparator.compare(arg0, (T)arg1);
	}
}
