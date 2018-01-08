package com.planet_ink.coffee_mud.core.collections;
import java.util.Enumeration;
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
public class ConvertingIterator<K, L> implements Iterator<L>
{
	private final Iterator<K> iterer;
	private K currObj = null;
	Converter<K, L> converter;

	public ConvertingIterator(Iterator<K> eset, Converter<K, L> conv)
	{
		iterer=eset;
		converter=conv;
	}

	@Override
	public boolean hasNext()
	{
		return (converter!=null) && iterer.hasNext();
	}

	@Override
	public L next()
	{
		if(!hasNext())
			throw new NoSuchElementException();
		currObj = iterer.next();
		return converter.convert(currObj);
	}

	@Override
	public void remove()
	{
		iterer.remove();
	}
}
