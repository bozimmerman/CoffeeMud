package com.planet_ink.coffee_mud.core.collections;

import java.util.List;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2022-2025 Bo Zimmerman

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
 * A SortedListWrap which uses the name of the CMObject to sort the list.
 *
 * @param <T> the type of object in the list
 */
public class CMUniqNameSortListWrapper<T extends CMObject> extends CMUniqSortListWrapper<T>
{
	/**
	 * Construct a new CMUniqNameSortListWrapper
	 *
	 * @param list the list to wrap
	 */
	public CMUniqNameSortListWrapper(final List<T> list)
	{
		super(list);
	}

	@Override
	protected int compareTo(final CMObject arg0, final String arg1)
	{
		return arg0.name().compareToIgnoreCase(arg1);
	}

	@Override
	protected int compareTo(final CMObject arg0, final CMObject arg1)
	{
		return arg0.name().compareToIgnoreCase(arg1.name());
	}

	@Override
	protected int compareToStarts(final CMObject arg0, final String arg1)
	{
		if(arg0.name().toLowerCase().startsWith(arg1.toLowerCase()))
			return 0;
		return arg0.name().compareToIgnoreCase(arg1);
	}
}
