package com.planet_ink.coffee_mud.core.collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
Copyright 2000-2014 Bo Zimmerman

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
public class MultiIterator<K> implements Iterator<K>
{
	private final Vector<Iterator<K>> iters=new Vector<Iterator<K>>();
	private volatile int dex=0;
	private volatile Iterator<K> iter=null;

	public MultiIterator(Iterator<K>[] esets)
	{
		if((esets==null)||(esets.length==0))
			return;
		for(final Iterator<K> I : esets)
			iters.add(I);
		setup();
	}

	public MultiIterator(Iterable<K>[] esets)
	{
		if((esets==null)||(esets.length==0))
			return;
		for(final Iterable<K> I : esets)
			iters.add(I.iterator());
		setup();
	}

	public MultiIterator(Iterable<? extends Iterable<K>> esets)
	{
		if(esets==null)
			return;
		for(final Iterable<K> I : esets)
			iters.add(I.iterator());
		setup();
	}


	public MultiIterator()
	{

	}

	public void add(Iterator<K> eset)
	{
		iters.add(eset);
		setup();
	}

	private void setup()
	{
		if((iter==null)&&(dex<iters.size()))
			iter=iters.get(dex);
		while((iter!=null)&&(!iter.hasNext())&&(++dex<iters.size()))
			iter=iters.get(dex);
	}

	@Override
	public boolean hasNext()
	{
		if(iter.hasNext()) return true;
		while((!iter.hasNext())&&(++dex<iters.size()))
			iter=iters.get(dex);
		return iter.hasNext();
	}

	@Override
	public K next()
	{
		if(!hasNext())
			throw new NoSuchElementException();
		return iters.get(dex).next();
	}

	@Override
	public void remove()
	{
		if(dex>=iters.size())
			throw new NoSuchElementException();
		iters.get(dex).remove();
	}
}
