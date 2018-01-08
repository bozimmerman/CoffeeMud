package com.planet_ink.coffee_mud.Abilities;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class ExtAbility extends StdAbility implements ExtendableAbility
{
	private String ID="ExtAbility";

	@Override
	public String ID()
	{
		return ID;
	}

	private StatsAffecting	statsAffector = null;
	private MsgListener		msgListener = null;
	private Tickable		tickable = null;
	
	@Override
	public ExtendableAbility setAbilityID(String ID) 
	{
		this.ID=ID;
		return this;
	}
	
	@Override
	public ExtendableAbility setStatsAffector(StatsAffecting code) 
	{
		this.statsAffector=code;
		return this;
	}
	
	@Override
	public ExtendableAbility setMsgListener(MsgListener code) 
	{
		this.msgListener=code;
		return this;
	}

	@Override
	public ExtendableAbility setTickable(Tickable code) 
	{
		this.tickable = code;
		return this;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(this.statsAffector != null)
			statsAffector.affectPhyStats(affected, affectableStats);
	}
	
	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		if(this.statsAffector != null)
			statsAffector.affectCharStats(affectedMob, affectableStats);
	}
	
	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		super.affectCharState(affectedMob, affectableMaxState);
		if(this.statsAffector != null)
			statsAffector.affectCharState(affectedMob, affectableMaxState);
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msgListener != null)
			msgListener.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msgListener != null)
			return msgListener.okMessage(myHost, msg);
		return true;
	}
	
	@Override
	public String name()
	{
		return (tickable != null) ? tickable.name() : super.name();
	}
	
	@Override
	public int getTickStatus()
	{
		return (tickable != null) ? tickable.getTickStatus() : super.getTickStatus();
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickable != null)
			return tickable.tick(ticking, tickID);
		return true;
	}
}
