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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class TaxiBehavior extends Concierge
{
	@Override public String ID(){return "TaxiBehavior";}
	@Override protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS;}
	protected final TrackingLibrary.TrackingFlags taxiTrackingFlags = new TrackingLibrary.TrackingFlags().plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
	
	protected TrackingLibrary.TrackingFlags getTrackingFlags()
	{
		return taxiTrackingFlags;
	}

	@Override
	public String accountForYourself()
	{
		return "taking you from here to there";
	}

	@Override
	protected String getGiveMoneyMessage(Environmental observer, Environmental destination, String moneyName)
	{
		if(observer instanceof MOB)
			return L("Yep, I can take you to @x1, but you'll need to give me @x2 first.",getDestinationName(destination),moneyName);
		else
		if(observer instanceof Container)
			return L("Yep, I can take you to @x1, but you'll need to put @x2 into @x3 first.",getDestinationName(destination),moneyName,observer.name());
		else
			return L("Yep, I can take you to @x1, but you'll need to drop @x2 first.",getDestinationName(destination),moneyName);
	}
	
	@Override
	protected void giveMerchandise(MOB whoM, Environmental destination, Environmental observer, Room room)
	{
		//TODO: start the car
	}
	
	@Override
	protected boolean disableComingsAndGoings()
	{
		return false; //TODO: only if moving
	}

	@Override
	protected void resetDefaults()
	{
		talkerName="the driver";
		greeting="Need a life? If so, just hop in.";
		mountStr="Where are you headed?";
		super.resetDefaults();
	}
	
	@Override
	public void setParms(String newParm)
	{
		super.setParms(newParm);
	}
}
