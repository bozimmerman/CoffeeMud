package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Diving extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Diving";
	}

	private final static String localizedName = CMLib.lang().L("Diving");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_FITNESS;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}
	
	protected volatile Boolean isDiving=null;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if((affected instanceof MOB)&&(isDiving!=null)&&(isDiving.booleanValue()))
		{
			affectableStats.addAmbiance("-FALLING");
			affectableStats.addAmbiance(L("Diving"));
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(CMLib.flags().isFalling(affected) && (affected instanceof MOB))
		{
			final Boolean isDiving=this.isDiving;
			if(isDiving == null)
			{
				if((affected.fetchEffect("Falling")!=null)
				&&(CMLib.flags().getFallingDirection(affected)==Directions.DOWN))
				{
					Room R=CMLib.map().roomLocation(affected);
					int tries=1000;
					while((R!=null)&&(!CMLib.flags().isWateryRoom(R))&&(--tries>0))
						R=R.getRoomInDir(Directions.DOWN);
					this.isDiving = (R==null) ? Boolean.FALSE : Boolean.TRUE;
				}
				else
					this.isDiving=Boolean.FALSE;
				if(this.isDiving.booleanValue())
					affected.recoverPhyStats();
			}
		}
		else
			isDiving=null;
		return true;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(isDiving!=null)
		&&(isDiving.booleanValue())
		&&(msg.tool() instanceof Ability)
		&&(msg.tool().ID().equals("Falling"))
		&&(CMLib.flags().isWateryRoom(CMLib.map().roomLocation(affected)))
		&&(msg.value()>0))
		{
			if(affected instanceof MOB)
				super.helpProficiency((MOB)affected, 0);
			msg.setValue(msg.value() - (int)Math.round(CMath.mul(msg.value(), CMath.div(proficiency(),100.0))));
		}
		return true;
	}
}
