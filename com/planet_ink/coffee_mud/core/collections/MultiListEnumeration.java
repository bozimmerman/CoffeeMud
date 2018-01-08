package com.planet_ink.coffee_mud.core.collections;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2011-2018 Bo Zimmerman

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
public class MultiListEnumeration<K> implements Enumeration<K>
{
	private final LinkedList<Iterable<K>> lists=new LinkedList<Iterable<K>>();
	private volatile Iterator<Iterable<K>> listIter = null;
	private volatile Iterator<K> iter = null;

	public MultiListEnumeration(final Iterable<K>[] esets)
	{
		if((esets!=null)&&(esets.length>0))
			lists.addAll(Arrays.asList(esets));
		setup(false);
	}

	public MultiListEnumeration(final List<List<K>> esetss, final boolean diffMethodSignature)
	{
		if((esetss!=null)&&(esetss.size()>0))
			lists.addAll(esetss);
		setup(false);
	}

	public MultiListEnumeration(final Iterable<K> eset)
	{
		lists.add(eset);
		setup(false);
	}

	private void setup(final boolean startOver)
	{
		if(startOver||(listIter==null))
			listIter=lists.iterator();
		if(startOver||(iter == null))
		{
			if(listIter.hasNext())
				iter=listIter.next().iterator();
		}
	}

	public void addEnumeration(List<K> set)
	{
		if(set != null)
			lists.add(set);
		setup(true);
	}

	@Override
	public boolean hasMoreElements()
	{
		boolean hasMore = (iter != null) && iter.hasNext();
		while(!hasMore)
		{
			if((listIter == null)||(!listIter.hasNext()))
			{
				iter=null;
				listIter = null;
				return false;
			}
			iter = listIter.next().iterator();
			hasMore = (iter != null) && iter.hasNext();
		}
		return hasMore;
	}

	@Override
	public K nextElement()
	{
		if(!hasMoreElements())
			throw new NoSuchElementException();
		return iter.next();
	}
}
