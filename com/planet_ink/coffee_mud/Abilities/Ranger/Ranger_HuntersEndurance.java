package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2014-2014 Bo Zimmerman

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
public class Ranger_HuntersEndurance extends StdAbility
{
	@Override public String ID() { return "Ranger_HuntersEndurance"; }
	private final static String localizedName = CMLib.lang().L("Hunters Endurance");
	@Override public String name() { return localizedName; }
	@Override public String displayText() { return ""; }
	@Override public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_FITNESS;}
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	@Override public int enchantQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public boolean isAutoInvoked(){return true;}
	@Override public boolean canBeUninvoked(){return false;}
	public volatile CharState oldState = null;
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null))
		{
			if((!CMLib.flags().canWorkOnSomething(affected))
			&&(CMLib.flags().isTracking((MOB)affected))
			&&(!((MOB)affected).isInCombat()))
			{
				if(oldState==null)
					oldState=(CharState)((MOB)affected).charStats().copyOf();
				((MOB)affected).curState().setFatigue(0);
				((MOB)affected).curState().setHunger(0);
				((MOB)affected).curState().setThirst(0);
				((MOB)affected).curState().setMovement(((MOB)affected).maxState().getMovement());
				if(CMLib.dice().rollPercentage()==1)
					super.helpProficiency((MOB)affected, 0);
			}
			else
			if(oldState!=null)
			{
				((MOB)affected).curState().setFatigue(oldState.getFatigue());
				((MOB)affected).curState().setHunger(oldState.getHunger());
				((MOB)affected).curState().setThirst(oldState.getThirst());
				((MOB)affected).curState().setMovement(oldState.getMovement());
				oldState = null;
			}
		}
		return true;
	}
}
