package com.planet_ink.coffee_mud.Abilities.Diseases;
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

public class Disease_SeaSickness extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_SeaSickness";
	}

	private final static String	localizedName	= CMLib.lang().L("Sea Sickness");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Sea Sickness)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 40;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 5;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("Your sea sickness subsides.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> suffers from sea sickness.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return L("<S-NAME> turn(s) green!");
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	@Override
	public int difficultyLevel()
	{
		return 0;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		final Room R=mob.location();
		if(R!=null)
		{
			if(R.getArea() instanceof BoardableShip)
			{
				final Item I=((BoardableShip)R.getArea()).getShipItem();
				final Room shipR=CMLib.map().roomLocation(I);
				if(!CMLib.flags().isWateryRoom(shipR))
				{
					unInvoke();
					return false;
				}
			}
			else
			if((mob.riding() !=null )
			&&(mob.riding().rideBasis()==Rideable.RIDEABLE_WATER))
			{
				if(!CMLib.flags().isWateryRoom(R))
				{
					unInvoke();
					return false;
				}
			}
			else
			{
				unInvoke();
				return false;
			}
			if((!mob.amDead())&&((--diseaseTick)<=0))
			{
				diseaseTick=DISEASE_DELAY();
				mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
				mob.curState().adjThirst(-1, mob.maxState().maxThirst(mob.baseWeight()));
				if(CMLib.dice().rollPercentage()==4)
				{
					mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> puke(s) -- BLEGHHHHHH!!!"));
					mob.curState().adjThirst(-100, mob.maxState().maxThirst(mob.baseWeight()));
				}
				return true;
			}
		}
		return true;
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null)
			return;
		affectableState.setMovement(affectableState.getMovement()/2);
		affectableState.setMana(affectableState.getMana()/2);
	}
}
