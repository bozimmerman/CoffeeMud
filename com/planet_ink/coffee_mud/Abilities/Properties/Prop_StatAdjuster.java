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
public class Prop_StatAdjuster extends Property
{
	@Override
	public String ID()
	{
		return "Prop_StatAdjuster";
	}

	@Override
	public String name()
	{
		return "Char Stats Adjusted MOB";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	protected static final int[]	all25	= new int[CharStats.CODES.instance().total()];
	static
	{
		for (final int i : CharStats.CODES.BASECODES())
			all25[i] = 0;
	}

	protected int[]					stats	= all25;

	@Override
	public boolean bubbleAffect()
	{
		return false;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ALWAYS;
	}

	@Override
	public String accountForYourself()
	{
		return "Stats Trainer";
	}

	protected boolean adjustMax=false;
	protected boolean doAllCodes=false;
	
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		for(final int i: CharStats.CODES.BASECODES())
		{
			if(stats[i]!=0)
			{
				int newStat=affectableStats.getStat(i)+stats[i];
				if(newStat<1)
					newStat=1;
				if(adjustMax)
					affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ+i, stats[i]);
				else
				{
					final int maxStat=affectableStats.getMaxStat(i);
					if(newStat>maxStat)
						newStat=maxStat;
				}
				affectableStats.setStat(i,newStat);
			}
		}
		if(doAllCodes)
		{
			for(final int i: CharStats.CODES.ALLCODES())
			{
				if((stats[i]!=0)&&(!CharStats.CODES.isBASE(i)))
				{
					affectableStats.setStat(i,affectableStats.getStat(i)+stats[i]);
				}
			}
		}
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			adjustMax=CMParms.getParmBool(newMiscText, "ADJMAX", false);
			stats=new int[CharStats.CODES.TOTAL()];
			for(final int i : CharStats.CODES.BASECODES())
				stats[i]=CMParms.getParmInt(newMiscText, CMStrings.limit(CharStats.CODES.NAME(i),3), 0);
			doAllCodes=false;
			for(final int i : CharStats.CODES.ALLCODES())
			{
				if(!CharStats.CODES.isBASE(i))
				{
					stats[i]=CMParms.getParmInt(newMiscText, CharStats.CODES.NAME(i), 0);
					if(stats[i]!=0)
						doAllCodes=true;
				}
			}
		}
	}

	@Override
	public String getStat(String code)
	{
		if((code!=null)&&(code.equalsIgnoreCase("LEVEL")))
		{
			int level = 0;
			return ""+level;
		}
		return super.getStat(code);
	}
	
	@Override
	public void setStat(String code, String val)
	{
		if((code!=null)&&(code.equalsIgnoreCase("LEVEL")))
		{
	
		}
		else
			super.setStat(code, val);
	}
}
