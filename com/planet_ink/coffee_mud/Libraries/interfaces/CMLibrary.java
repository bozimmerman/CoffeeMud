package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
   Copyright 2000-2015 Bo Zimmerman

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
public interface CMLibrary extends CMObject
{
	public boolean activate();
	public boolean shutdown();
	public void propertiesLoaded();
	public TickClient getServiceClient();

	/**
	 * Localize an internal string -- shortcut. Same as calling:
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary#fullSessionTranslation(String, String...)
	 * Call with the string to translate, which may contain variables of the form @x1, @x2, etc. The array in xs
	 * is then used to replace the variables AFTER the string is translated.
	 * @param str the string to translate
	 * @param xs the array of variables to replace
	 * @return the translated string, with all variables in place
	 */
	public String L(final String str, final String ... xs);
}
