package com.planet_ink.coffee_mud.core.collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2010-2025 Bo Zimmerman

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
 * An enumeration that converts the objects in a backing enumeration from one
 * type to another using a Converter.
 *
 * @param <K> the backing collection type
 * @param <L> the outward facing collection type
 */
public class ConvertingEnumeration<K, L> implements Enumeration<L>
{
	private final Enumeration<K> enumer;
	Converter<K, L> converter;

	/**
	 * Construct a new ConvertingEnumeration
	 *
	 * @param eset the backing enumeration
	 * @param conv the converter
	 */
	public ConvertingEnumeration(final Enumeration<K> eset, final Converter<K, L> conv)
	{
		enumer=eset;
		converter=conv;
	}

	/**
	 * Construct a new ConvertingEnumeration
	 *
	 * @param eset the backing iterator
	 * @param conv the converter
	 */
	public ConvertingEnumeration(final Iterator<K> eset, final Converter<K, L> conv)
	{
		enumer=new IteratorEnumeration<K>(eset);
		converter=conv;
	}
	/**
	 * Set the converter for this enumeration
	 * @param conv the new converter
	 */
	public void setConverter(final Converter<K, L> conv)
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
