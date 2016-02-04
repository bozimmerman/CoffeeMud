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
import com.planet_ink.coffee_mud.Items.interfaces.SpaceShip.ShipFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2016 Bo Zimmerman

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


public class GravityFloat extends StdAbility
{
	@Override
	public String ID()
	{
		return "GravityFloat";
	}

	private final static String	localizedName	= CMLib.lang().L("GravityFloat");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Floating)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS | Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}
	
	private class PossiblyFloater implements Runnable
	{
		private final Physical P;
		private final boolean hasGrav;

		public PossiblyFloater(final Physical possFloater, final boolean hasGravity)
		{
			this.P = possFloater;
			this.hasGrav = hasGravity;
		}

		@Override
		public void run()
		{
			final boolean hasGravity = confirmGravity(P,hasGrav);
			final Ability gravA=P.fetchEffect("GravityFloat");
			if(hasGravity)
			{
				if((gravA!=null)&&(!gravA.isSavable()))
				{
					gravA.unInvoke();
					P.delEffect(gravA);
				}
			}
			else
			{
				if(gravA==null)
				{
					Ability gravityA=(Ability)copyOf();
					if(gravityA != null)
					{
						P.addNonUninvokableEffect(gravityA);
						gravityA.setSavable(false);
					}
				}
			}
		}
	}
	
	private final Runnable checkStopFloating = new Runnable()
	{
		@Override
		public void run()
		{
			final Physical P = affected;
			if(P!=null)
			{
				if(confirmGravity(P, false))
				{
					unInvoke();
					P.delEffect(GravityFloat.this);
				}
			}
		}
	};
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Tickable.TICKID_MOB)
			return true;

		checkStopFloating.run();
		return true;
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setWeight(1);
		affectableStats.addAmbiance("Floating");
	}
	
	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		// TODO: getting, putting, are harder
		// TODO: entering, leaving, mounting very difficult
		// TODO: swimming should sorta work, if they try it
		// TODO: pushing sometimes works
		// TODO: gravity legs should develop over time...this turns into a saved ability with a score?
		return true;
	}
	
	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		// TODO: throwing is a way to move from room to room now!
		// TODO: pushing sometimes works
		// TODO: gravity legs should develop over time...this turns into a saved ability with a score?
		if(affected instanceof Item)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
				if(msg.target()==affected)
					msg.addTrailerRunnable(checkStopFloating);
				break;
			}
		}
		else
		if(affected instanceof MOB)
		{
			
		}
	}
	
	protected boolean confirmGravity(final Physical P, boolean hasGravity)
	{
		if(P instanceof Item)
		{
			final Item I=(Item)P;
			if((I.container()!=null)
			||(I.owner() instanceof MOB)
			||(I instanceof ShipComponent))
			{
				if(!hasGravity)
					hasGravity=true;
			}
		}
		else
		if(P instanceof MOB)
		{
			final MOB M=(MOB)P;
			if((M.riding() != null) 
			|| (CMLib.flags().isBound(M)))
			{
				if(!hasGravity)
					hasGravity=true;
			}
			final Area A=CMLib.map().areaLocation(M);
			if(A instanceof SpaceShip)
			{
				if(!((SpaceShip)A).getShipFlag(ShipFlag.NO_GRAVITY))
				{
					if(!hasGravity)
						hasGravity=true;
				}
			}
		}
		return hasGravity;
	}
	
	
	@Override
	public boolean invoke(final MOB mob, List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(givenTarget==null)
			return false;
		final Physical P = givenTarget;
		
		new PossiblyFloater(P, auto).run();
		
		return true;
	}
}
