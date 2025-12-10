package com.planet_ink.coffee_mud.core.interfaces;

/*
Copyright 2025-2025 Bo Zimmerman

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
* Interface for things which automatically bundle with other like
* things, to save references.
*
* @author Bo Zimmerman
*
*/
public interface AutoBundler extends Contingent
{
	/**
	 * Cause this item to automatically join like items.
	 * This might cause it to be destroyed.
	 * @return
	 */
	public boolean autoBundle();

	/**
	 * Returns the size of the bundle.
	 *
	 * @return the size of the bundle.
	 */
	public int getBundleSize();

	/**
	 * Sets a new size for the bundle.  A value less than
	 * or equal to 0 may delete it.
	 * @param size the new size
	 */
	public void setBundleSize(int size);
}
