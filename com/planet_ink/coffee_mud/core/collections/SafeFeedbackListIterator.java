package com.planet_ink.coffee_mud.core.collections;

import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

/*
	Copyright 2015-2018 Bo Zimmerman
	
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

public class SafeFeedbackListIterator<K> implements ListIterator<K> 
{
	private final ListIterator<K> iter;
	private final SafeCollectionHost collection;
	private final AtomicBoolean returned = new AtomicBoolean(false);

	public SafeFeedbackListIterator(ListIterator<K> iter, SafeCollectionHost collection) 
	{
		this.iter = iter;
		this.collection = collection;
		this.collection.submitIterator(this);
	}

	@Override
	public void finalize() throws Throwable
	{
		if(!returned.get())
		{
			collection.returnIterator(this);
			returned.set(true);
		}
		super.finalize();
	}
	
	@Override
	public boolean hasNext() 
	{
		return iter.hasNext();
	}

	@Override
	public K next() 
	{
		return iter.next();
	}

	@Override
	public void remove() 
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean hasPrevious() {
		return iter.hasPrevious();
	}

	@Override
	public K previous() {
		return iter.previous();
	}

	@Override
	public int nextIndex() {
		return iter.nextIndex();
	}

	@Override
	public int previousIndex() {
		return iter.previousIndex();
	}

	@Override
	public void set(K e) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void add(K e) {
		throw new java.lang.UnsupportedOperationException();
	}

}
