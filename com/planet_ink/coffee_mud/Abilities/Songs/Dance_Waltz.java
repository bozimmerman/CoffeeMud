package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Dance_Waltz extends Dance
{
	@Override
	public String ID()
	{
		return "Dance_Waltz";
	}

	private final static String localizedName = CMLib.lang().L("Waltz");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	private int[] statadd=null;

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		if(statadd==null)
		{
			statadd=new int[CharStats.CODES.TOTAL()];
			int classLevel=CMLib.ableMapper().qualifyingClassLevel(invoker(),this)+(3*getXLEVELLevel(invoker()));
			classLevel=(classLevel+1)/9;
			classLevel++;

			for(int i=0;i<classLevel;i++)
				statadd[CharStats.CODES.BASECODES()[CMLib.dice().roll(1,CharStats.CODES.BASECODES().length,-1)]]+=3;
		}
		for(final int i: CharStats.CODES.BASECODES())
			affectedStats.setStat(i,affectedStats.getStat(i)+statadd[i]);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		statadd=null;
		return super.invoke(mob,commands,givenTarget,auto,asLevel);
	}

}
