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
public class EnumerationIterator<K> implements Iterator<K>
{
	private final Enumeration<K> e;

	@SuppressWarnings("unchecked")
	public EnumerationIterator(Enumeration<K> e)
	{
		if(e==null)
			this.e=EmptyEnumeration.INSTANCE;
		else
			this.e=e;
		hasNext();
	}

	@Override
	public boolean hasNext()
	{
		return e.hasMoreElements();
	}

	@Override
	public K next()
	{
		return e.nextElement();
	}

	@Override
	public void remove()
	{
		throw new java.lang.UnsupportedOperationException();
	}

}
