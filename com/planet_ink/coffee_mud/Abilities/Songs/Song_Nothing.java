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
   Copyright 2001-2018 Bo Zimmerman

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

public class Song_Nothing extends Song
{
	@Override
	public String ID()
	{
		return "Song_Nothing";
	}

	private final static String localizedName = CMLib.lang().L("Nothing");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected boolean skipStandardSongInvoke()
	{
		return true;
	}

	public Song_Nothing()
	{
		super();
		setProficiency(100);
	}

	@Override
	public void setProficiency(int newProficiency)
	{
		super.setProficiency(100);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		boolean foundOne=false;
		for(int a=0;a<mob.numEffects();a++)
		{
			final Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Song))
				foundOne=true;
		}
		unsingAllByThis(mob,mob);
		if(!foundOne)
		{
			mob.tell(auto?L("There is no song playing."):L("You aren't singing."));
			return true;
		}

		mob.location().show(mob,null,CMMsg.MSG_NOISE,auto?L("Silence."):L("<S-NAME> stop(s) singing."));
		mob.location().recoverRoomStats();
		return true;
	}
}
