package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2013-2015 Bo Zimmerman

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
public class Prop_StatAdjuster extends Property
{
	@Override public String ID() { return "Prop_StatAdjuster"; }
	@Override public String name(){ return "Char Stats Adjusted MOB";}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected static final int[] all25=new int[CharStats.CODES.instance().total()];
	static { for(final int i : CharStats.CODES.BASECODES()) all25[i]=0;}
	protected int[] stats=all25;
	@Override public boolean bubbleAffect(){return false;}
	@Override public long flags(){return Ability.FLAG_ADJUSTER;}
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ALWAYS;
	}

	@Override
	public String accountForYourself()
	{ return "Stats Trainer";	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		for(final int i: CharStats.CODES.BASECODES())
			if(stats[i]!=0)
			{
				int newStat=affectableStats.getStat(i)+stats[i];
				final int maxStat=affectableStats.getMaxStat(i);
				if(newStat>maxStat)
					newStat=maxStat;
				else
				if(newStat<1)
					newStat=1;
				affectableStats.setStat(i,newStat);
			}
	}
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			stats=new int[CharStats.CODES.TOTAL()];
			for(final int i : CharStats.CODES.BASECODES())
				stats[i]=CMParms.getParmInt(newMiscText, CMStrings.limit(CharStats.CODES.NAME(i),3), 0);
		}
	}

}
