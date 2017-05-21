package com.planet_ink.coffee_mud.Abilities.Languages;

import com.planet_ink.coffee_mud.core.CMLib;

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
