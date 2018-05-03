package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
@SuppressWarnings("rawtypes")
public class EmptyIterator<K> implements Iterator<K>
{
	private EmptyIterator()
	{
	}

	@Override
	public boolean hasNext()
	{
		return false;
	}

	@Override
	public K next()
	{
		throw new NoSuchElementException();
	}

	@Override
	public void remove()
	{
		throw new NoSuchElementException();
	}

	public static final Iterator		INSTANCE	    = new EmptyIterator();
	public static final Iterator<String>STRINSTANCE	= new EmptyIterator<String>();
}
