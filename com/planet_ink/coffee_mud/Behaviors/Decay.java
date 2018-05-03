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
   Copyright 2002-2018 Bo Zimmerman

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
public class Decay extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "Decay";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ITEMS | Behavior.CAN_MOBS;
	}

	public Decay()
	{
		super();
		minTicks = 50;
		maxTicks = 50;
		chance = 100;
		tickReset();
	}

	protected boolean	activated	= false;
	protected String	answer		= " vanishes!";

	@Override
	public String accountForYourself()
	{
		return "decaying over time";
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		activated=false;
		tickDown=CMParms.getParmInt(parms,"remain",tickDown);
		answer=CMParms.getParmStr(parms,"answer"," vanishes!");
		if(newParms.toUpperCase().indexOf("NOTRIGGER")>=0)
			activated=true;
	}

	@Override
	public String getParms()
	{
		final String s=parms;
		final int x=s.toUpperCase().indexOf("REMAIN=");
		if(x<0)
			return "remain="+tickDown+" "+s;
		int y=s.indexOf(' ',x+1);
		if(y<0)
			y=s.length();
		return ("remain="+tickDown+" "+s.substring(0,x)+s.substring(y).trim()).trim();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(!activated)
			return true;
		if(canAct(ticking,tickID))
		{
			if(ticking instanceof MOB)
			{
				final MOB mob=(MOB)ticking;
				final Room room=mob.location();
				if(room!=null)
				{
					if(mob.amDead())
						mob.setLocation(null);
					mob.destroy();
					room.recoverRoomStats();
					if(answer.trim().length()>0)
						room.showHappens(CMMsg.MSG_OK_VISUAL,mob.name()+" "+answer.trim());
				}
			}
			else
			if(ticking instanceof Item)
			{
				final Item item=(Item)ticking;
				final Environmental E=item.owner();
				if(E==null)
					return true;
				final Room room=getBehaversRoom(ticking);
				if(room==null)
					return true;
				if(answer.trim().length()>0)
				{
					if(E instanceof MOB)
					{
						((MOB)E).tell(item.name()+" "+answer.trim());
						((MOB)E).recoverPhyStats();
						((MOB)E).recoverCharStats();
						((MOB)E).recoverMaxState();
					}
					else
					if(E instanceof Room)
						((Room)E).showHappens(CMMsg.MSG_OK_VISUAL,item.name()+" "+answer.trim());
				}
				item.destroy();
				room.recoverRoomStats();
			}
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if(activated)
			return;
		if(msg.amITarget(affecting))
		{
			if(affecting instanceof Rideable)
			{
				if(((msg.targetMinor()==CMMsg.TYP_SLEEP)
					||(msg.targetMinor()==CMMsg.TYP_SIT)
					||(msg.targetMinor()==CMMsg.TYP_MOUNT)
					||(msg.targetMinor()==CMMsg.TYP_ENTER))
				&&(!msg.source().isMonster())
				&&(CMLib.masking().maskCheck(getParms(),msg.source(),true)))
					activated=true;
			}
			else
			if(affecting instanceof MOB)
			{
				if((msg.targetMajor(CMMsg.MASK_MALICIOUS))
				&&(!msg.source().isMonster())
				&&(CMLib.masking().maskCheck(getParms(),msg.source(),true)))
					activated=true;
			}
			else
			if((affecting instanceof Armor)
			||(affecting instanceof Weapon))
			{
				if(((msg.targetMinor()==CMMsg.TYP_WEAR)
					||(msg.targetMinor()==CMMsg.TYP_HOLD)
					||(msg.targetMinor()==CMMsg.TYP_WIELD))
				&&(CMLib.masking().maskCheck(getParms(),msg.source(),true)))
					activated=true;
			}
			else
			if(affecting instanceof Item)
			{
				if(((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
				&&(CMLib.masking().maskCheck(getParms(),msg.source(),true)))
				{
					activated=true;
				}
			}
		}
	}
}
