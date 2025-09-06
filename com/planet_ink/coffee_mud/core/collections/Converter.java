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
 * A simple interface for converting objects of one type to another.
 *
 * @param <K> the input object type
 * @param <L> the output object type
 */
public interface Converter<K, L>
{
	/**
	 * Converts the given object from one type to another.
	 *
	 * @param obj the object to be converted
	 * @return the converted object
	 */
	public L convert(K obj);

	/**
	 * A converter that converts strings to lower case.
	 */
	public final static Converter<String,String> toLowerCase=new Converter<String,String>()
	{
		@Override
		public String convert(final String obj)
		{
			return obj.toLowerCase();
		}
	};

	/**
	 * A converter that converts strings to upper case.
	 */
	public final static Converter<String,String> toUpperCase=new Converter<String,String>()
	{
		@Override
		public String convert(final String obj)
		{
			return obj.toUpperCase();
		}
	};

}
