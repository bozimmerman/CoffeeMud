package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2012-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, e\ither express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdFactoryMOB extends StdMOB
{
	@Override
	public String ID()
	{
		return "StdFactoryMOB";
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdFactoryMOB();
	}

	@Override
	protected void finalize() throws Throwable
	{
		if(!amDestroyed)
			destroy();
		amDestroyed=false;
		if(!CMClass.returnMob(this))
		{
			amDestroyed=true;
			super.finalize();
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			CharStats savedCStats=charStats;
			if(charStats==baseCharStats)
				savedCStats=(CharStats)CMClass.getCommon("DefaultCharStats");
			PhyStats savedPStats=phyStats;
			if(phyStats==basePhyStats)
				savedPStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
			final CharState savedCState=curState;
			if((curState==baseState)||(curState==maxState))
				curState=(CharState)CMClass.getCommon("DefaultCharState");
			super.destroy();
			removeFromGame=false;
			charStats=savedCStats;
			phyStats=savedPStats;
			curState=savedCState;
			baseCharStats.reset();
			basePhyStats.reset();
			baseState.reset();
			maxState.reset();
			curState.reset();
			phyStats.reset();
			charStats.reset();
			finalize();
		}
		catch(final Throwable t)
		{
			Log.errOut(ID(),t);
		}
	}
}
