package com.planet_ink.coffee_mud.Exits;
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
import com.planet_ink.coffee_mud.Locales.ClimbableSurface;
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
public class ClimbableExit extends StdExit
{
	@Override
	public String ID()
	{
		return "ClimbableExit";
	}

	@Override
	public String Name()
	{
		return "a sheer surface";
	}

	@Override
	public String displayText()
	{
		return "a sheer surface";
	}

	@Override
	public String description()
	{
		return "Looks like you'll have to climb it.";
	}

	protected Ability climbA;

	public ClimbableExit()
	{
		super();
		climbA=CMClass.getAbility("Prop_Climbable");
		if(climbA!=null)
		{
			climbA.setAffectedOne(this);
			climbA.makeNonUninvokable();
		}
		recoverPhyStats();
	}
	
	@Override
	public CMObject copyOf()
	{
		final ClimbableExit R = (ClimbableExit)super.copyOf();
		R.climbA=CMClass.getAbility("Prop_Climbable");
		R.climbA.setAffectedOne(R);
		R.climbA.makeNonUninvokable();
		return R;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((climbA!=null)&&(!climbA.okMessage(myHost, msg)))
			return false;
		return super.okMessage(myHost, msg);
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(climbA!=null)
			climbA.executeMsg(myHost, msg);
		super.executeMsg(myHost,msg);
	}
	
	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(climbA!=null)
			climbA.affectPhyStats(this, phyStats());
	}
}
