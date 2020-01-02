package com.planet_ink.coffee_mud.Abilities.Languages;

import com.planet_ink.coffee_mud.core.CMLib;

/*
Copyright 2014-2020 Bo Zimmerman

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
public class WormSpeak extends AnimalSpeak
{
	@Override
	public String ID()
	{
		return "WormSpeak";
	}

	private final static String localizedName = CMLib.lang().L("Worm Speak");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String[] animalSounds=
	{
		"mrrr","mrrmrmmmrmrm","mmmmm","rrrrm","rr","m","mrmrmrmrm"
	};

	@Override
	protected String[] getSounds()
	{
		return animalSounds;
	}
}
