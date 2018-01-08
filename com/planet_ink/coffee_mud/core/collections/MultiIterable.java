package com.planet_ink.coffee_mud.core.collections;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2013-2018 Bo Zimmerman

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
public class MultiIterable<K> implements Iterable<K>, SizedIterable<K>
{
	@SuppressWarnings("unchecked")
	private Iterable<K>[] iters=new Iterable[0];
	private int size=0;

	public MultiIterable(Iterable<K>[] esets, int newSize)
	{
		if((esets==null)||(esets.length==0))
			return;
		iters=esets.clone();
		size=newSize;
	}

	public MultiIterable(Collection<? extends Iterable<? extends K>> esets, int newSize)
	{
		if((esets==null)||(esets.size()==0))
			return;
		iters=esets.toArray(iters);
		size=newSize;
	}

	public MultiIterable()
	{
	}

	public synchronized void add(Iterable<K> eset, int sizeAdd)
	{
		iters=Arrays.copyOf(iters, iters.length+1);
		iters[iters.length-1]=eset;
		size+=sizeAdd;
	}

	@Override
	public Iterator<K> iterator()
	{
		return new MultiIterator<K>(iters);
	}

	@Override
	public int size()
	{
		return size;
	}

}
