package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2022-2023 Bo Zimmerman

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
public class ListGetIterator<J> implements Iterator<J>
{
	private volatile int	i = -1;
	private final List<J>	coll;
	private final Object	removerContext;

	private final ListIteratorRemover<Object, J>	remover;

	public static interface ListIteratorRemover<K,J>
	{
		public void remove(final K context, final J j);
	}

	public ListGetIterator(final List<J> col)
	{
		coll = col;
		remover=null;
		removerContext=null;
	}

	public ListGetIterator(final List<J> col, final ListIteratorRemover<Object,J> rem, final Object removerContext)
	{
		this.coll = col;
		this.remover=rem;
		this.removerContext = removerContext;
	}

	@Override
	public boolean hasNext()
	{
		return i<coll.size()-1;
	}

	@Override
	public J next()
	{
		synchronized(coll)
		{
			try
			{
				return coll.get(++i);
			}
			catch(final Exception e)
			{
				return null;
			}
		}
	}

	@Override
	public void remove()
	{
		synchronized(coll)
		{
			if((i>=0)&&(i<coll.size()))
			{
				try
				{
					final J j = coll.remove(i);
					i--;
					if(remover != null)
						remover.remove(removerContext, j);
				}
				catch(final Exception e)
				{}
			}
		}
	}
}
