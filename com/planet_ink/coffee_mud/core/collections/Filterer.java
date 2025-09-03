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
/**
 * A simple functional interface for filtering objects.
 *
 * @param <K> the type of object to be filtered
 */
public interface Filterer<K>
{
	/**
	 * Returns true if the given object passes the filter.
	 *
	 * @param obj the object to be filtered
	 * @return true if the given object passes the filter
	 */
	public boolean passesFilter(K obj);

	// common filterers

	/**
	 * A filterer that accepts anything.
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static final Filterer ANYTHING=new Filterer()
	{
		/**
		 * Returns true for any given object.
		 * @param obj the object to be filtered
		 * @return true for any given object
		 */
		@Override
		public boolean passesFilter(final Object obj)
		{
			return true;
		}
	};
	/**
	 * A filterer that accepts only non-null objects.
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static final Filterer NON_NULL=new Filterer()
	{
		/**
		 * Returns true if the given object is non-null.
		 * @param obj the object to be filtered
		 * @return true if the given object is non-null
		 */
		@Override
		public boolean passesFilter(final Object obj)
		{
			return obj != null;
		}
	};

	/**
	 * A filterer that negates a given filterer.
	 *
	 */
	public static class NotFilterer<L> implements Filterer<L>
	{
		private final Filterer<L> filter;

		/**
		 * Constructs a NotFilterer.
		 *
		 * @param filter the filter to be negated
		 */
		public NotFilterer(final Filterer<L> filter)
		{
			this.filter = filter;
		}

		/**
		 * Returns true if the given object does NOT pass the given filter.
		 *
		 * @param obj the object to be filtered
		 * @return true if the given object does NOT pass the given filter
		 */
		@Override
		public boolean passesFilter(final L obj)
		{
			return !filter.passesFilter(obj);
		}
	}

	/**
	 * A filterer that filters strings based on whether they contain a given
	 * substring, with optional case insensitivity.
	 *
	 */
	public static class TextFilter implements Filterer<String>
	{
		final String filter;
		final boolean caseInsensitive;

		/**
		 * Constructs a TextFilter.
		 *
		 * @param str the substring to filter on
		 * @param caseInsensitive true to ignore case, false otherwise
		 */
		public TextFilter(final String str, final boolean caseInsensitive)
		{
			this.caseInsensitive=caseInsensitive;
			if(this.caseInsensitive)
				this.filter=str.toLowerCase();
			else
				this.filter=str;
		}

		/**
		 * Returns true if the given string contains the filter substring.
		 *
		 * @param obj the string to be filtered
		 * @return true if the given string contains the filter substring
		 */
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
