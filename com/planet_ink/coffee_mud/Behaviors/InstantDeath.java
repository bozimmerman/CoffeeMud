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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class InstantDeath extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "InstantDeath";
	}

	@Override
	public long flags()
	{
		return super.flags()|Behavior.FLAG_POTENTIALLYAUTODEATHING;
	}

	protected CompiledZMask mask=null;

	public InstantDeath()
	{
		super();
		minTicks=1;maxTicks=1;chance=100;
		tickReset();
	}

	@Override
	public void setParms(String parms)
	{
		super.setParms(parms);
		final String maskStr=CMParms.getParmStr(parms,"mask","");
		mask=null;
		if((maskStr!=null)&&(maskStr.trim().length()>0))
			mask=CMLib.masking().getPreCompiledMask(maskStr);
	}

	boolean activated=false;

	@Override
	public String accountForYourself()
	{
		return "instant killing";
	}

	public void killEveryoneHere(MOB spareMe, Room R)
	{
		if(R==null)
			return;
		final Vector<MOB> V=new Vector<MOB>();
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((spareMe!=null)&&(spareMe==M))
				continue;
			if((M!=null)
			&&(!CMSecurity.isAllowed(M,R,CMSecurity.SecFlag.IMMORT))
			&&((mask==null)||(CMLib.masking().maskCheck(mask, M, false))))
				V.addElement(M);
		}
		for(int v=0;v<V.size();v++)
		{
			final MOB M=V.elementAt(v);
			CMLib.combat().postDeath(null,M,null);
		}
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
					killEveryoneHere(mob,room);
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
				if((E instanceof MOB)&&((mask==null)||(CMLib.masking().maskCheck(mask, E, false))))
					CMLib.combat().postDeath(null,(MOB)E,null);
				else
				if(E instanceof Room)
					killEveryoneHere(null,(Room)E);
				room.recoverRoomStats();
			}
			else
			if(ticking instanceof Room)
				killEveryoneHere(null,(Room)ticking);
			else
			if(ticking instanceof Area)
			{
				for(final Enumeration<Room> r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					killEveryoneHere(null,R);
				}
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
			if(affecting instanceof MOB)
			{
				if((msg.targetMajor(CMMsg.MASK_MALICIOUS))
				&&(!msg.source().isMonster()))
					activated=true;
			}
			else
			if((affecting instanceof Food)
			||(affecting instanceof Drink))
			{
				if((msg.targetMinor()==CMMsg.TYP_EAT)
				||(msg.targetMinor()==CMMsg.TYP_DRINK))
					activated=true;
			}
			else
			if((affecting instanceof Armor)
			||(affecting instanceof Weapon))
			{
				if((msg.targetMinor()==CMMsg.TYP_WEAR)
				||(msg.targetMinor()==CMMsg.TYP_HOLD)
				||(msg.targetMinor()==CMMsg.TYP_WIELD))
					activated=true;
			}
			else
			if(affecting instanceof Item)
			{
				if((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
					activated=true;
			}
			else
				activated=true;
		}
	}
}
