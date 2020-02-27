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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2020 Bo Zimmerman

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
public class VeryAggressive extends Aggressive
{
	@Override
	public String ID()
	{
		return "VeryAggressive";
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
	}

	public VeryAggressive()
	{
		super();

		tickWait = 0;
		tickDown = 0;
	}

	@Override
	public String accountForYourself()
	{
		if(getParms().trim().length()>0)
			return "surprising aggression against "+CMLib.masking().maskDesc(getParms(),true).toLowerCase();
		else
			return "surprising aggressiveness";
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		tickDown=tickWait;
	}

	@Override
	public boolean grantsAggressivenessTo(final MOB M)
	{
		return CMLib.masking().maskCheck(getParms(),M,false);
	}

	public void tickVeryAggressively(final Tickable ticking, final int tickID,
									 final boolean wander, final boolean mobKiller,
									 final boolean misBehave, final boolean levelCheck,
									 final MaskingLibrary.CompiledZMask mask, final String attackMsg,
									 final boolean noGangUp)
	{
		if(tickID!=Tickable.TICKID_MOB)
			return;
		if(!canFreelyBehaveNormal(ticking))
			return;
		final MOB mob=(MOB)ticking;
		if(CMLib.flags().isATrackingMonster(mob))
			return;

		// ridden things dont wander!
		if(ticking instanceof Rideable)
		{
			if(((Rideable)ticking).numRiders()>0)
				return;
		}

		if(((mob.amFollowing()!=null)
			&&(CMLib.tracking().areNearEachOther(mob,mob.amFollowing())
				||CMLib.tracking().areNearEachOther(mob,mob.amUltimatelyFollowing())))
		||(!CMLib.flags().canTaste(mob)))
			return;

		// let's not do this 100%
		if(CMLib.dice().rollPercentage()>15)
			return;

		final Room R=mob.location();
		if((R.getArea()!=null)
		&&(R.getArea().getAreaState()!=Area.State.ACTIVE))
			return;
		for(int m=0;m<R.numInhabitants();m++)
		{
			final MOB inhab=R.fetchInhabitant(m);
			if((inhab!=null)
			&&(CMSecurity.isAllowed(inhab,R,CMSecurity.SecFlag.ORDER))
			&&(CMSecurity.isAllowed(inhab,R,CMSecurity.SecFlag.CMDROOMS)))
				return;
		}

		int dirCode=-1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room room=R.getRoomInDir(d);
			final Exit exit=R.getExitInDir(d);
			if((room!=null)
			&&(exit!=null)
			&&(wander||room.getArea().Name().equals(R.getArea().Name())))
			{
				if(exit.isOpen())
				{
					for(int i=0;i<room.numInhabitants();i++)
					{
						final MOB inhab=room.fetchInhabitant(i);
						if((inhab!=null)
						&&((!inhab.isMonster())||(mobKiller))
						&&(CMLib.flags().canSenseEnteringLeaving(inhab,mob))
						&&((!levelCheck)||(mob.phyStats().level()<(inhab.phyStats().level()+5)))
						&&(CMLib.masking().maskCheck(mask,inhab,false))
						&&(((mask!=null)&&(mask.entries().length>0))
							||((inhab.phyStats().level()<(mob.phyStats().level()+8))
								&&(inhab.phyStats().level()>(mob.phyStats().level()-8)))))
						{
							dirCode=d;
							break;
						}
					}
				}
			}
			if(dirCode>=0)
				break;
		}
		if((dirCode>=0)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MOBILITY)))
		{
			CMLib.tracking().walk(mob,dirCode,false,false);
			pickAFight(mob,mask,mobKiller,misBehave,levelCheck,attackMsg,noGangUp);
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickVeryAggressively(ticking,tickID,
								 wander,
								 mobkill,
								 misbehave,
								 this.levelcheck,
								 this.mask,
								 attackMessage,
								 noGangUp);
		}
		return true;
	}
}
