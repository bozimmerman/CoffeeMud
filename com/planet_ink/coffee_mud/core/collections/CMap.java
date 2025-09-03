package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

/*
   Copyright 2016-2025 Bo Zimmerman

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
 * A map which provides enumerations of its keys and values.
 *
 * @param <K> the key type
 * @param <F> the value type
 * @author Bo Zimmerman
 *
 */
public interface CMap<K,F> extends Map<K,F>
{
	/**
	 * Returns an enumeration of the values in this map.
	 *
	 * @return an enumeration of the values in this map
	 */
	public Enumeration<F> elements();

	/**
	 * Returns an enumeration of the keys in this map.
	 *
	 * @return an enumeration of the keys in this map
	 */
	public Enumeration<K> keys();
}
