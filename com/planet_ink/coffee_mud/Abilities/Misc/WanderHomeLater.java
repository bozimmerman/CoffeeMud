package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2012-2018 Bo Zimmerman

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

public class WanderHomeLater extends StdAbility
{
	@Override
	public String ID()
	{
		return "WanderHomeLater";
	}

	private final static String	localizedName	= CMLib.lang().L("WanderHomeLater");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Waiting til you're clear to go home)");

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

	private boolean	areaOk			= false;
	private boolean	ignorePCs		= false;
	private boolean	ignoreFollow	= false;
	private boolean	once			= false;
	private boolean	destroy			= false;
	private boolean	respectFollow	= false;
	private int		currentWait		= 0;
	private int		minTicks		= 0;
	private int		maxTicks		= 0;

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		areaOk=CMParms.getParmBool(newMiscText,"areaok", areaOk);
		destroy=CMParms.getParmBool(newMiscText,"destroy", destroy);
		ignorePCs=CMParms.getParmBool(newMiscText,"ignorepcs", ignorePCs);
		ignoreFollow=CMParms.getParmBool(newMiscText,"ignorefollow", ignoreFollow);
		respectFollow=CMParms.getParmBool(newMiscText,"respectfollow", ignoreFollow);
		once=CMParms.getParmBool(newMiscText,"once", once);
		minTicks=CMParms.getParmInt(newMiscText, "minticks", minTicks);
		maxTicks=CMParms.getParmInt(newMiscText, "maxticks", maxTicks);
		currentWait = minTicks + ((maxTicks <= minTicks)?0:CMLib.dice().roll(1, maxTicks-minTicks, 0));
	}
	
	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		super.unInvoke();
		if((P!=null)&&(this.canBeUninvoked)&&(this.unInvoked))
		{
			if((!P.amDestroyed())
			&&(destroy)
			&&(CMLib.flags().isInTheGame(P, true))) 
			{
				final Room R=CMLib.map().roomLocation(P);
				if((R!=null)&&(!((MOB)P).amDead()))
					R.showHappens(CMMsg.MSG_OK_ACTION, P,L("<S-NAME> wander(s) off."));
				P.destroy();
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			if(currentWait > 0)
			{
				currentWait--;
				if(currentWait > 0)
					return super.tick(ticking, tickID);
				currentWait = minTicks + ((maxTicks <= minTicks)?0:CMLib.dice().roll(1, maxTicks-minTicks, 0));
			}
			if((once) && (M.getStartRoom()==M.location()))
				unInvoke();
			else
			if(M.amDead() && (once))
				unInvoke();
			else
			if(respectFollow && (M.amFollowing()!=null) && (M.amFollowing().location()==M.location()))
				unInvoke();
			else
			if(CMLib.flags().canActAtAll(M)
			&&(!M.isInCombat())
			&&(ignoreFollow || (M.amFollowing()==null)))
			{
				if(M.getStartRoom()!=null)
				{
					final Room startRoom= M.getStartRoom();
					final Room curRoom=M.location();
					
					if(areaOk && (startRoom != null) && (curRoom != null))
					{
						if(startRoom.getArea() == curRoom.getArea())
							return super.tick(ticking, tickID);
					}
					
					if(startRoom != curRoom)
						CMLib.tracking().wanderAway(M, !ignorePCs, true);
					if(once)
					{
						if(startRoom==M.location())
							unInvoke();
					}
				}
				else
				{
					unInvoke();
				}
			}
		}
		return super.tick(ticking, tickID);
	}
}
