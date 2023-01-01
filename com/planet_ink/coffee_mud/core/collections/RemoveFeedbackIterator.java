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
public class RemoveFeedbackIterator<J> implements Iterator<J>
{
	private final Iterator<J>	iter;
	private final Object		removerContext;
	private volatile J			lastNext = null;

	private final FeedbackIteratorRemover<Object, J>	remover;

	public static interface FeedbackIteratorRemover<K,J>
	{
		public void remove(final K context, final J j);
	}

	public RemoveFeedbackIterator(final Iterator<J> iter)
	{
		this.iter = iter;
		remover=null;
		removerContext=null;
	}

	public RemoveFeedbackIterator(final Iterator<J> iter, final FeedbackIteratorRemover<Object,J> rem, final Object removerContext)
	{
		this.iter = iter;
		this.remover=rem;
		this.removerContext = removerContext;
	}

	@Override
	public boolean hasNext()
	{
		return iter.hasNext();
	}

	@Override
	public J next()
	{
		lastNext=iter.next();
		return lastNext;
	}

	@Override
	public void remove()
	{
		if(lastNext != null)
		{
			iter.remove();
			try
			{
				if(remover != null)
					remover.remove(removerContext, lastNext);
			}
			catch(final Exception e)
			{}
			lastNext=null;
		}
	}
}
