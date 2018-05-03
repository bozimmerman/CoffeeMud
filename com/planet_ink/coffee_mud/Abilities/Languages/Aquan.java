package com.planet_ink.coffee_mud.Abilities.Languages;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Aquan extends AnimalSpeak
{
	@Override
	public String ID()
	{
		return "Aquan";
	}

	private final static String	localizedName	= CMLib.lang().L("Aquan");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String[] soundBase = new String[] 
	{
		"gurgle","blub","eeeeeeeee","oioioi","glub",
		"honk","glugglge","mrrr","wurrr","llllgl","gl","lb",
		"llrrrwwwrrr","blip","flup","glglglll","wwwrrr","lllrr",
		"glug","blubbablup","gurglflub","blubllll","splurt",
		"oi","eeee","rrwwwll","onkglgl","bluggg","lrrr","lg" 
	};

	private static String[] animalSounds = null;
	
	@Override
	protected String[] getSounds() 
	{
		if(animalSounds == null)
		{
			List<String> sounds=new XVector<String>(soundBase);
			Random r=new Random(System.currentTimeMillis());
			for(int i=0;i<soundBase.length * 2;i++)
			{
				String s=soundBase[r.nextInt(soundBase.length)]+soundBase[r.nextInt(soundBase.length)];
				if(!sounds.contains(s))
					sounds.add(s);
				else
					i--;
			}
			animalSounds = sounds.toArray(new String[sounds.size()]);
		}
		return animalSounds;
	}
}
