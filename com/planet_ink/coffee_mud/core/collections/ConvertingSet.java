package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

/*
   Copyright 2020-2025 Bo Zimmerman

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
 * A simple Set implementation that converts the objects in a backing collection
 * from one type to another using a Converter.
 *
 * @param <L> the backing collection type
 * @param <K> the outward facing collection type
 */
public class ConvertingSet<L,K> extends ConvertingCollection<L,K> implements Set<K>
{
	/**
	 * Construct a new ConvertingSet
	 * @param l the backing collection
	 * @param conv the converter
	 */
	public ConvertingSet(final Collection<L> l, final Converter<L, K> conv)
	{
		super(l,conv);
	}
}
