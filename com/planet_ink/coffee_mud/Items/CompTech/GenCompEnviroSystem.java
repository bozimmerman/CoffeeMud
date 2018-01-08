package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class GenCompEnviroSystem extends GenElecCompItem
{
	@Override
	public String ID()
	{
		return "GenCompEnviroSystem";
	}

	protected final static int ENVIRO_TICKS=7;
	
	protected int	tickDown	= ENVIRO_TICKS;
	protected int	airResource	= RawMaterial.RESOURCE_AIR;

	public GenCompEnviroSystem()
	{
		super();
		setName("a generic environment system");
		setDisplayText("a generic environment system sits here.");
		setDescription("");
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_ENVIRO_CONTROL;
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(L("@x1 is currently @x2",name(),(activated()?"operating.\n\r":"deactivated/disconnected.\n\r")));
				return;
			case CMMsg.TYP_POWERCURRENT:
				if(activated())
				{
					if(--tickDown <=0)
					{
						tickDown=ENVIRO_TICKS;
						final SpaceObject obj=CMLib.map().getSpaceObject(this, true);
						if(obj instanceof SpaceShip)
						{
							final SpaceShip ship=(SpaceShip)obj;
							final Area A=ship.getShipArea();
							double pct= 1.0;
							if(subjectToWearAndTear())
								pct=pct*CMath.div(usesRemaining(),100);
							if(CMSecurity.isDebugging(DbgFlag.SPACESHIP))
								Log.debugOut("Refreshing the air in "+ship.Name());
							final String code=Technical.TechCommand.AIRREFRESH.makeCommand(Double.valueOf(pct),Integer.valueOf(airResource));
							final CMMsg msg2=CMClass.getMsg(msg.source(), ship, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
							if(A.okMessage(msg2.source(), msg2))
								A.executeMsg(msg2.source(), msg2);
						}
					}
				}
				break;
			}
		}
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenCompEnviroSystem))
			return false;
		return super.sameAs(E);
	}
}
