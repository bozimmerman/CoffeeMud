package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
 * This is the base interface for all CoffeeMud libraries.
 * @author Bo Zimmerman
 */
public interface CMLibrary extends CMObject
{
	/**
	 * Activates the library.  This is called after the mud
	 * is booted, but before connections are accepted.
	 * @see CMLibrary#shutdown()
	 * @return true if activation was successful, false if you're screwed
	 */
	public boolean activate();
	
	/**
	 * Shuts down the library.  Called at system shutdown time
	 * obviously, but is sometimes called just to reset the library.
	 * @see CMLibrary#activate()
	 * @return true if shutdown was successful, false if there's nothing you can do about it
	 */
	public boolean shutdown();
	
	/**
	 * This method is called whenever system properties are altered by the user.  This
	 * allows the library to react to any important properties they monitor.
	 */
	public void propertiesLoaded();
	
	/**
	 * If this library has a service thread, this method returns the TickClient
	 * object associated with that service.  Normally returns null, since most
	 * libraries don't set themselves up to receive thread time.
	 * @see com.planet_ink.coffee_mud.core.interfaces.TickClient
	 * @return null, or the TickClient for the service
	 */
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
