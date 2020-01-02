package com.planet_ink.coffee_mud.core.collections;

import java.util.*;
/*
   Copyright 2012-2020 Bo Zimmerman

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

import com.planet_ink.coffee_mud.core.Log;
/*
Copyright 2000-2020 Bo Zimmerman

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
public final class ReadOnlyVector<T> extends Vector<T>
{
	private static final long	serialVersionUID	= -9175373358592311411L;

	public ReadOnlyVector()
	{
		super();
	}

	public ReadOnlyVector(final List<T> E)
	{
		if (E != null)
			super.addAll(E);
	}

	public ReadOnlyVector(final T[] E)
	{
		if (E != null)
		{
			for (final T o : E)
				super.add(o);
		}
	}

	public ReadOnlyVector(final T E)
	{
		if (E != null)
			super.add(E);
	}

	public ReadOnlyVector(final Enumeration<T> E)
	{
		if (E != null)
		{
			for (; E.hasMoreElements();)
				super.add(E.nextElement());
		}
	}

	public ReadOnlyVector(final Iterator<T> E)
	{
		if (E != null)
		{
			for (; E.hasNext();)
				super.add(E.next());
		}
	}

	public ReadOnlyVector(final Set<T> E)
	{
		for (final T o : E)
			super.add(o);
	}

	public ReadOnlyVector(final int size)
	{
		super(size);
	}

	@Override
	public synchronized boolean add(final T t)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
		return false;
	}

	@Override
	public synchronized void addElement(final T t)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
	}

	@Override
	public synchronized void removeElementAt(final int index)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
	}

	@Override
	public synchronized boolean removeElement(final Object obj)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
		return false;
	}

	@Override
	public boolean remove(final Object o)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
		return false;
	}

	@Override
	public void add(final int index, final T element)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
	}

	@Override
	public synchronized T remove(final int index)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
		return null;
	}

	@Override
	public void clear()
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
	}

	@Override
	public synchronized boolean removeAll(final Collection<?> c)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
		return false;
	}

	@Override
	public synchronized void insertElementAt(final T obj, final int index)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
	}

	@Override
	public synchronized boolean addAll(final Collection<? extends T> c)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
		return false;
	}

	@Override
	public synchronized boolean addAll(final int index, final Collection<? extends T> c)
	{
		Log.errOut("ReadOnlyVector", new UnsupportedOperationException());
		return false;
	}
}
