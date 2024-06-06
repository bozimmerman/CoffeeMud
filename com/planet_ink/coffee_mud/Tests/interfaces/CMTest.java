package com.planet_ink.coffee_mud.Tests.interfaces;

import java.util.List;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
Copyright 2024-2024 Bo Zimmerman

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
* Interface for a test, typically a unit test of some sort.
*
* @see com.planet_ink.coffee_mud.core.CMClass
*
* @author Bo Zimmerman
*/
public interface CMTest extends CMObject
{
	/**
	 * Returns a list of test group names that this test belongs to.
	 *
	 * @return the list of tests, or an empty list
	 */
	public String[] getTestGroups();

	/**
	 * If a test has any cleanup required, it will do it here.
	 */
	public void cleanupTest();

	/**
	 * Perform this test.
	 *
	 * @param mob mob to inform of problems
	 * @param metaFlags room the mob is in
	 * @param what the word used to invoke the test
	 * @param commands args to the test, if any
	 * @return null, or an error message if it failed
	 */
	public String doTest(final MOB mob, final int metaFlags, String what, final List<String> commands);
}
