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

public class Prop_ReqNoMOB extends Property implements TriggeredAffect
{
	@Override
	public String ID()
	{
		return "Prop_ReqNoMOB";
	}

	@Override
	public String name()
	{
		return "Monster Limitations";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;
	}

	private boolean noFollow=false;
	private boolean noSneak=false;

	@Override
	public long flags()
	{
		return Ability.FLAG_ZAPPER;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public void setMiscText(String txt)
	{
		noFollow=false;
		noSneak=false;
		final Vector<String> parms=CMParms.parse(txt.toUpperCase());
		String s;
		for(final Enumeration<String> p=parms.elements();p.hasMoreElements();)
		{
			s=p.nextElement();
			if("NOFOLLOW".startsWith(s))
				noFollow=true;
			else
			if(s.startsWith("NOSNEAK"))
				noSneak=true;
		}
		super.setMiscText(txt);
	}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null)
			return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(!noSneak))
			return true;
		return !mob.isMonster();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
		   ||((msg.target() instanceof Rideable)&&(msg.targetMinor()==CMMsg.TYP_SIT)))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area))
		&&(!CMLib.flags().isFalling(msg.source())))
		{
			final HashSet<MOB> H=new HashSet<MOB>();
			if(noFollow)
				H.add(msg.source());
			else
			{
				msg.source().getGroupMembers(H);
				int hsize=0;
				while(hsize!=H.size())
				{
					hsize=H.size();
					final HashSet<MOB> H2=new XHashSet<MOB>(H);
					for(final Iterator<MOB> e=H2.iterator();e.hasNext();)
					{
						final MOB M=e.next();
						M.getRideBuddies(H);
					}
				}
				if(msg.source().isMonster()
				&&(msg.source().riding() instanceof BoardableShip))
				{
					final Area subA=((BoardableShip)msg.source().riding()).getShipArea();
					for(Enumeration<Room> r=subA.getProperMap();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						if(R!=null)
						{
							for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if(M!=null)
									H.add(M);
							}
						}
					}
				}
			}
			for (final Object O : H)
			{
				if((!(O instanceof MOB))||(passesMuster((MOB)O)))
					return super.okMessage(myHost,msg);
			}
			msg.source().tell(L("You are not allowed in there."));
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
