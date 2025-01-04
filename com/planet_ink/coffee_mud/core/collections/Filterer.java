package com.planet_ink.coffee_mud.core.collections;

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
public interface Filterer<K>
{
	public boolean passesFilter(K obj);

	@SuppressWarnings("rawtypes")
	public static final Filterer ANYTHING=new Filterer()
	{
		@Override
		public boolean passesFilter(final Object obj)
		{
			return true;
		}
	};

	@SuppressWarnings("rawtypes")
	public static final Filterer NON_NULL=new Filterer()
	{
		@Override
		public boolean passesFilter(final Object obj)
		{
			return obj != null;
		}
	};

	public static class NotFilterer<L> implements Filterer<L>
	{
		private final Filterer<L> filter;
		public NotFilterer(final Filterer<L> filter)
		{
			this.filter = filter;
		}
		@Override
		public boolean passesFilter(final L obj)
		{
			return !filter.passesFilter(obj);
		}
	}

	public static class TextFilter implements Filterer<String>
	{
		final String filter;
		final boolean caseInsensitive;
		public TextFilter(final String str, final boolean caseInsensitive)
		{
			this.caseInsensitive=caseInsensitive;
			if(this.caseInsensitive)
				this.filter=str.toLowerCase();
			else
				this.filter=str;
		}
		@Override
		public boolean passesFilter(final String obj)
		{
			if(caseInsensitive)
				return obj.toLowerCase().indexOf(obj)>=0;
			else
				return obj.indexOf(obj)>=0;
		}
	}
}
