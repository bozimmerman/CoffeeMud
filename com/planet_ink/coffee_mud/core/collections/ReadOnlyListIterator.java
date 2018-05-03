package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
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
public class ReadOnlyListIterator<K> implements ListIterator<K>
{
	private final ListIterator<K> iter;

	public ReadOnlyListIterator(ListIterator<K> i)
	{
		iter=i;
	}

	@Override
	public void add(K arg0)
	{
		iter.add(arg0);
	}

	@Override
	public boolean hasNext()
	{
		return iter.hasNext();
	}

	@Override
	public boolean hasPrevious()
	{
		return iter.hasPrevious();
	}

	@Override
	public K next()
	{
		return iter.next();
	}

	@Override
	public int nextIndex()
	{
		return iter.nextIndex();
	}

	@Override
	public K previous()
	{
		return iter.previous();
	}

	@Override
	public int previousIndex()
	{
		return iter.previousIndex();
	}

	@Override
	public void remove()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void set(K arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

}
