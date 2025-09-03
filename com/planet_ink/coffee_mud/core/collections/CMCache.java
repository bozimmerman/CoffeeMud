package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

/*
   Copyright 2021-2025 Bo Zimmerman

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
 * A cache is a collection that can be efficiently enumerated, and whose
 * elements can be marked as recently used.
 *
 * @param <K> the type of object in the cache
 */
public interface CMCache<K> extends Collection<K>
{
	/**
	 * Returns an enumeration of the elements in this cache.
	 *
	 * @return an enumeration of the elements in this cache
	 */
	public Enumeration<K> elements();

	/**
	 * Marks the given element as recently used.
	 *
	 * @param k the element to mark as recently used
	 */
	public void touch(K k);
}
