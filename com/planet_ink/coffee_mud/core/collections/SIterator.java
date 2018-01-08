package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/*
   Copyright 2012-2018 Bo Zimmerman

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
public class SIterator<K> implements Iterator<K>
{
	private final Iterator<K>	iter;
	private K	              o	= null;

	public SIterator(final Iterator<K> i)
	{
		iter = i;
		nextUp();
	}

	private void nextUp()
	{
		try
		{
			if (iter.hasNext())
			{
				o = iter.next();
				return;
			}
		}
		catch (final Exception e)
		{

		}
		o = null;
	}

	@Override
	public boolean hasNext()
	{
		return o != null;
	}

	@Override
	public K next()
	{
		final K o2 = o;
		nextUp();
		return o2;
	}

	@Override
	public void remove()
	{
		throw new java.lang.IllegalArgumentException();
	}
}
