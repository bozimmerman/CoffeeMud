package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Enumeration;

import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/*
   Copyright 2010-2018 Bo Zimmerman

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
*
* A Physical object capable of initiating an effect on the world.
* (Even if it is normally passive)
* @see com.planet_ink.coffee_mud.core.interfaces.Physical
* @see com.planet_ink.coffee_mud.core.interfaces.Behavable
*
* @author Bo Zimmerman
*
*/
public interface PhysicalAgent extends Physical, Behavable
{
}
