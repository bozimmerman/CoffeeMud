package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClayGolem extends StoneGolem
{
	public String ID(){	return "ClayGolem"; }
	public String name(){ return "Clay Golem"; }
	protected static Vector resources=new Vector();
	
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("a pound of clay",EnvResource.RESOURCE_CLAY));
				resources.addElement(makeResource
					("essence of golem",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
