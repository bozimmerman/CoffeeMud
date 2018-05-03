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
public class ConvertingListIterator<K,L> implements ListIterator<L>
{
	private final ListIterator<K> iter;
	private final Converter<K,L> converter;
	public ConvertingListIterator(ListIterator<K> i, Converter<K,L> conv)
	{
		iter=i;
		converter=conv;
	}

	@Override
	public void add(L arg0)
	{
		throw new java.lang.IllegalArgumentException();
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
	public L next()
	{
		return converter.convert(iter.next());
	}

	@Override
	public int nextIndex()
	{
		return iter.nextIndex();
	}

	@Override
	public L previous()
	{
		return converter.convert(iter.previous());
	}

	@Override
	public int previousIndex()
	{
		return iter.previousIndex();
	}

	@Override
	public void remove()
	{
		iter.remove();
	}

	@Override
	public void set(L arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

}
