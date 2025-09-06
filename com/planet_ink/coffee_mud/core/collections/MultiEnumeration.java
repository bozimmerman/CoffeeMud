package com.planet_ink.coffee_mud.core.collections;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
 * An enumeration that is made up of multiple enumerations.
 *
 * @param <K> the type of object being enumerated
 */
public class MultiEnumeration<K> implements Enumeration<K>
{
	private final LinkedList<Enumeration<? extends K>> enums=new LinkedList<Enumeration<? extends K>>();
	private volatile Enumeration<? extends K> enumer=null;

	/**
	 * A builder interface for constructing a multi-enumeration
	 *
	 * @param <K> the type of object being enumerated
	 */
	public static interface MultiEnumeratorBuilder<K>
	{
		/**
		 * Returns the multi-enumeration being built
		 * @return the multi-enumeration being built
		 */
		public MultiEnumeration<K> getList();
	}

	/**
	 * Construct a new multi-enumeration
	 *
	 * @param esets the enumerations to include
	 */
	public MultiEnumeration(final Collection<Enumeration<K>> esets)
	{
		if(esets!=null)
			enums.addAll(esets);
	}

	/**
	 * Construct a new multi-enumeration
	 */
	public MultiEnumeration()
	{
	}

	/**
	 * Construct a new multi-enumeration
	 *
	 * @param eset the enumeration to include
	 */
	public MultiEnumeration(final Enumeration<K> eset)
	{
		enums.add(eset);
	}

	/**
	 * Construct a new multi-enumeration
	 *
	 * @param esets the enumerations to include
	 */
	public MultiEnumeration(final Enumeration<? extends K>[] esets)
	{
		if(esets != null)
		{
			for(final Enumeration<? extends K> e: esets)
				addEnumeration(e);
		}
	}

	/**
	 * Adds a new enumeration
	 *
	 * @param set the enumerations to add
	 * @return this object
	 */
	public MultiEnumeration<K> addEnumeration(final Enumeration<? extends K> set)
	{
		if(set != null)
			enums.add(set);
		return this;
	}

	@Override
	public boolean hasMoreElements()
	{
		boolean hasMore = (enumer != null) && enumer.hasMoreElements();
		while(!hasMore)
		{
			if(enums.size()==0)
			{
				enumer=null;
				return false;
			}
			enumer=enums.removeFirst();
			hasMore = (enumer != null) && enumer.hasMoreElements();
		}
		return hasMore;
	}

	@Override
	public K nextElement()
	{
		if(!hasMoreElements())
			throw new NoSuchElementException();
		return enumer.nextElement();
	}
}
