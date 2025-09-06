package com.planet_ink.coffee_mud.core.collections;
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
 * A converter which can convert in both directions, and also takes a cardinal
 * value for the forward conversion.
 *
 * @param <K> the input type
 * @param <L> the output type
 */
public interface FullConverter<K, L>
{
	/**
	 * Converts the given object of type K into an object of type L, using the
	 * given cardinal value to influence the conversion.
	 *
	 * @param cardinal a cardinal value to influence the conversion
	 * @param obj the object to convert
	 * @return the converted object
	 */
	public L convert(int cardinal, K obj);

	/**
	 * Converts the given object of type L back into an object of type K.
	 *
	 * @param obj the object to convert
	 * @return the converted object
	 */
	public K reverseConvert(L obj);
}
