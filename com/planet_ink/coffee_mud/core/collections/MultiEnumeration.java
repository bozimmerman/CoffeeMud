package com.planet_ink.coffee_mud.core.collections;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
public class MultiEnumeration<K> implements Enumeration<K>
{
	private final LinkedList<Enumeration<K>> enums=new LinkedList<Enumeration<K>>();
	private volatile Enumeration<K> enumer=null;

	public static interface MultiEnumeratorBuilder<K>
	{
		public MultiEnumeration<K> getList();
	}
	
	public MultiEnumeration(Enumeration<K>[] esets)
	{
		if((esets!=null)&&(esets.length>0))
			for(final Enumeration<K> E : esets)
				if(E!=null)
					enums.add(E);
	}

	public MultiEnumeration(Collection<Enumeration<K>> esets)
	{
		if(esets!=null)
			enums.addAll(esets);
	}

	public MultiEnumeration(Enumeration<K> eset)
	{
		enums.add(eset);
	}

	public MultiEnumeration<K> addEnumeration(Enumeration<K> set)
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
