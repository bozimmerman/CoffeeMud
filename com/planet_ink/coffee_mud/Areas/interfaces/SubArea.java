package com.planet_ink.coffee_mud.Areas.interfaces;

import java.util.List;
import java.util.Map;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.interfaces.*;
/*
   Copyright 2011-2020 Bo Zimmerman

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
 * SubAreas are areas that are derived from other existing areas, whether thin or no.
 * @author Bo Zimmerman
 */
public interface SubArea extends Area
{
	/**
	 * The area that this one is a "sub" of.
	 * This is very different from parent areas.
	 *
	 * @return the super area
	 */
	public Area getSuperArea();
}
