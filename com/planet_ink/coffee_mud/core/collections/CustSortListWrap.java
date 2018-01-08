package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class CustSortListWrap<T extends Comparable<T>> extends SortedListWrap<T>
{
	private final Comparator<T> comparator;
	public CustSortListWrap(List<T> list, Comparator<T> comparator)
	{
		super(list);
		this.comparator=comparator;
	}

	@SuppressWarnings("unchecked")

	@Override protected int compareTo(T arg0, Object arg1)
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
