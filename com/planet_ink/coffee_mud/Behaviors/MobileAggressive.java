package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
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
public class MobileAggressive extends Mobile
{
	@Override
	public String ID()
	{
		return "MobileAggressive";
	}

	protected int	tickWait	= 0;

	@Override
	public long flags()
	{
		return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
	}

	protected boolean			mobkill				= false;
	protected boolean			misbehave			= false;
	protected String			attackMsg			= null;
	protected int				aggressiveTickDown	= 0;
	protected boolean			levelcheck			= false;
	protected VeryAggressive	veryA				= new VeryAggressive();
	protected CompiledZMask		mask				= null;

	public MobileAggressive()
	{
		super();

		tickDown = 0;
		aggressiveTickDown = 0;
	}

	@Override
	public String accountForYourself()
	{
		if(getParms().trim().length()>0)
			return "wandering aggression against "+CMLib.masking().maskDesc(getParms(),true).toLowerCase();
		else
			return "wandering aggressiveness";
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		attackMsg=CMParms.getParmStr(newParms,"MESSAGE",null);
		tickDown=tickWait;
		aggressiveTickDown=tickWait;
		final Vector<String> V=CMParms.parse(newParms.toUpperCase());
		levelcheck=V.contains("CHECKLEVEL");
		mobkill=V.contains("MOBKILL");
		misbehave=V.contains("MISBEHAVE");
		this.mask=CMLib.masking().getPreCompiledMask(newParms);
	}

	@Override
	public boolean grantsAggressivenessTo(MOB M)
	{
		if(M==null)
			return true;
		return CMLib.masking().maskCheck(getParms(),M,false);
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		veryA.executeMsg(affecting, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_MISC+0;
		super.tick(ticking,tickID);
		tickStatus=Tickable.STATUS_MISC+1;
		if(tickID!=Tickable.TICKID_MOB)
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		if((--aggressiveTickDown)<0)
		{
			aggressiveTickDown=tickWait;
			tickStatus=Tickable.STATUS_MISC+2;
			veryA.tickAggressively(ticking,tickID,mobkill,misbehave,levelcheck,this.mask,attackMsg);
			tickStatus=Tickable.STATUS_MISC+3;
			veryA.tickVeryAggressively(ticking,tickID,wander,mobkill,misbehave,levelcheck,this.mask,attackMsg);
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
