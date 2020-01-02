package com.planet_ink.coffee_mud.core.collections;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2012-2020 Bo Zimmerman

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
public class CMUniqNameSortSVec<T extends CMObject> extends CMUniqSortSVec<T>
{
	/**
	 *
	 */
	private static final long	serialVersionUID	= 5770001849890830938L;

	public CMUniqNameSortSVec(final int size)
	{
		super(size);
	}

	public CMUniqNameSortSVec()
	{
		super();
	}

	public CMUniqNameSortSVec(final CMUniqNameSortSVec<T> O)
	{
		super();
		for(final T o: O)
			this.add(o);
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

	@SuppressWarnings("unchecked")

	@Override
	public synchronized SVector<T> copyOf()
	{
		try
		{
			return (CMUniqNameSortSVec<T>) clone();
		}
		catch (final Exception e)
		{
			return new CMUniqNameSortSVec<T>(this);
		}
	}

}
