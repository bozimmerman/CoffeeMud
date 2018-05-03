package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_SneakAttack extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SneakAttack";
	}

	private final static String localizedName = CMLib.lang().L("Sneak Attack");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
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

	protected boolean activated=false;
	protected boolean oncePerRound=false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(activated)
		{
			final double prof=(proficiency())/100.0;
			final double xlvl=super.getXLEVELLevel(invoker());
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round((((affectableStats.damage())/4.0)+xlvl)*prof));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round((50.0+(10.0*xlvl))*prof));
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected==null)||((!(affected instanceof MOB))))
			return true;
		if(activated
		   &&(!oncePerRound)
		   &&msg.amISource((MOB)affected)
		   &&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			oncePerRound=true;
			helpProficiency((MOB)affected, 0);
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(CMLib.flags().isHidden(affected))
		{
			if(!activated)
			{
				activated=true;
				affected.recoverPhyStats();
			}
		}
		else
		if(activated)
		{
			activated=false;
			affected.recoverPhyStats();
		}
		if(oncePerRound)
			oncePerRound=false;
		return super.tick(ticking,tickID);
	}

}
