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
public class ConvertingEnumeration<K, L> implements Enumeration<L>
{
	private final Enumeration<K> enumer;
	Converter<K, L> converter;

	public ConvertingEnumeration(Enumeration<K> eset, Converter<K, L> conv)
	{
		enumer=eset;
		converter=conv;
	}

	public ConvertingEnumeration(Iterator<K> eset, Converter<K, L> conv)
	{
		enumer=new IteratorEnumeration<K>(eset);
		converter=conv;
	}

	public void setConverter(Converter<K, L> conv)
	{
		converter=conv;
	}

	@Override
	public boolean hasMoreElements()
	{
		return (converter!=null) && enumer.hasMoreElements();
	}

	@Override
	public L nextElement()
	{
		if(!hasMoreElements())
			throw new NoSuchElementException();
		return converter.convert(enumer.nextElement());
	}
}
