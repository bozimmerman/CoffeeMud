package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prop_ClosedSeason extends Property
{
	public String ID() { return "Prop_ClosedSeason"; }
	public String name(){ return "Contingent Visibility";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_MOBS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	private Vector closedV=null;
	boolean doneToday=false;
	private Area exitArea=null;

	public String accountForYourself()
	{ return "";	}

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		closedV=Util.parse(text.toUpperCase());
	}

	public void executeMsg(Environmental E, CMMsg msg)
	{
		super.executeMsg(E,msg);
		if(exitArea!=null) return;
		if(!(affected instanceof Exit)) return;
		if(msg.source().location()!=null)
			exitArea=msg.source().location().getArea();
	}
	
	private boolean closed(Area A)
	{
		if(A==null) return false;
		
		for(int i=0;i<Room.variationCodes.length;i++)
		{
			if(closedV.contains(Room.variationCodes[i][0]))
			{
				int num=Util.s_int(Room.variationCodes[i][1].substring(1));
				switch(Room.variationCodes[i][1].charAt(0))
				{
				case 'W':
					if(A.getClimateObj().weatherType(null)==num)
						return true;
					break;
				case 'C':
					if(A.getTimeObj().getTODCode()==num)
						return true;
					break;
				case 'S':
					if(A.getTimeObj().getSeasonCode()==num)
						return true;
					break;
				}
			}
		}
		return false;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected==null) return;
		if((affected instanceof MOB)||(affected instanceof Item))
		{
			Room R=CoffeeUtensils.roomLocation(affected);
			if((R!=null)
			&&(closed(R.getArea()))
			&&((!(affected instanceof MOB))||(!((MOB)affected).isInCombat())))
			{
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_NOT_SEEN);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
			}
		}
		else
		if((affected instanceof Room)&&(closed(((Room)affected).getArea())))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_DARK);
		else
		if(affected instanceof Exit)
		{
			if(closed(exitArea==null?CMMap.getFirstArea():exitArea))
			{
				if(!doneToday)
				{
					doneToday=true;
					Exit e=((Exit)affected);
					e.setDoorsNLocks(e.hasADoor(),false,e.defaultsClosed(),e.hasALock(),e.hasALock(),e.defaultsLocked());
				}
			}
			else
			{
				if(doneToday)
				{
					doneToday=false;
					Exit e=((Exit)affected);
					e.setDoorsNLocks(e.hasADoor(),!e.defaultsClosed(),e.defaultsClosed(),e.hasALock(),e.defaultsLocked(),e.defaultsLocked());
				}
			}
		}

	}
}
