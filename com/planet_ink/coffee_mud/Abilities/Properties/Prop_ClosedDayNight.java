package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ClosedDayNight extends Property
{
	private boolean doneToday=false;
	public Prop_ClosedDayNight()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Day/Night Visibility";
		canAffectCode=Ability.CAN_ITEMS|Ability.CAN_MOBS|Ability.CAN_EXITS;
	}

	public Environmental newInstance()
	{
		Prop_ClosedDayNight newOne=new Prop_ClosedDayNight();
		newOne.setMiscText(text());
		return newOne;
	}

	public String accountForYourself()
	{ return "";	}


	private boolean closed()
	{
		boolean closed=(CMMap.getArea(0).getTODCode()==Area.TIME_NIGHT);
		if(text().equalsIgnoreCase("DAY")) closed=!closed;
		return closed;
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected==null) return;
		if((affected instanceof MOB)
		||(affected instanceof Item))
		{
			if(closed())
			{
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SEEN);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_MOVE);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SPEAK);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_HEAR);
			}
		}
		else
		if((affected instanceof Room)&&(closed()))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_DARK);
		else
		if(affected instanceof Exit)
		{
			if(closed())
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
					e.setDoorsNLocks(e.hasADoor(),e.defaultsClosed(),e.defaultsClosed(),e.hasALock(),e.defaultsLocked(),e.defaultsLocked());
				}
			}
		}
		
	}
}
